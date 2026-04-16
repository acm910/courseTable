package com.example.coursetable.feature.webView

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 用 WebView 完成登录态，然后用 OkHttp 带 Cookie 拉课表接口 JSON。
 *
 * @param entryUrl 打开教务系统入口页；登录后会自动从 currentUser 接口解析学号和学年学期
 * @param onKcbJson 成功拿到课表接口原始 JSON 回调
 * @param onLog 输出调试日志（可接到你自己的 logger）
 * @param onError 任何错误回调
 */



@Composable
@SuppressLint("SetJavaScriptEnabled")
fun JwxtKcbWebView(
    onKcbJson: (String) -> Unit,
    modifier: Modifier = Modifier,
    entryUrl: String = "https://jwxt.whut.edu.cn/jwapp/sys/yjsrzfwapp/dbLogin/main.do",
    onCloseWebView: () -> Unit = {},
    onLog: (String) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    // 防止 onPageFinished 多次触发导致重复拉接口
    val hasFetched = remember { mutableStateOf(false) }
    val isRequestInFlight = remember { mutableStateOf(false) }

    // OkHttpClient 可复用
    val client = remember {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    // 每次进入导入页都重置抓取状态，避免上次流程残留。
    LaunchedEffect(Unit) {
        hasFetched.value = false
        isRequestInFlight.value = false
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef.value = this
                // WebView 基础设置
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE

                // 如果你担心站点根据 UA 做兼容，可以加一个标记
                settings.userAgentString = settings.userAgentString + " JwxtKcbClient/1.0"

                // 每次启动都清空 WebView 缓存/历史，保证是新的一次页面会话。
                clearWebViewCacheOnly(this, onLog)

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)
                cookieManager.flush()

                webChromeClient = object : WebChromeClient() {}

                webViewClient = object : WebViewClient() {

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLog("onPageStarted: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLog("onPageFinished: $url")
                        if (hasFetched.value || isRequestInFlight.value) return

                        // 从 jwapp 根路径取 cookie（关键：这里能拿到 GS_SESSIONID/_WEU 等）
                        val cm = CookieManager.getInstance()
                        val cookie = cm.getCookie("https://jwxt.whut.edu.cn/jwapp/")

                        if (cookie.isNullOrBlank()) {
                            onLog("Cookie empty yet; wait for further navigation.")
                            return
                        }

                        // 简单判断：有会话 cookie 才尝试拉课表（避免未登录时误请求）
                        val looksLoggedIn =
                            cookie.contains("GS_SESSIONID=") || cookie.contains("_WEU=")

                        if (!looksLoggedIn) {
                            onLog("Cookie does not look logged in yet; waiting user login.")
                            return
                        }

                        isRequestInFlight.value = true
                        onLog("Cookie acquired, fetching timetable...")

                        scope.launch(Dispatchers.IO) {
                            runCatching {
                                val currentUser = fetchCurrentUserInfo(
                                    client = client,
                                    cookieHeader = cookie,
                                    onLog = onLog
                                )
                                fetchKcbDataJson(
                                    client = client,
                                    cookieHeader = cookie,
                                    xnxqdm = currentUser.xnxqdm,
                                    xh = currentUser.xh
                                )
                            }.onSuccess { kcbDataJson ->
                                withContext(Dispatchers.Main) {
                                    hasFetched.value = true
                                    isRequestInFlight.value = false
                                    onKcbJson(kcbDataJson)
                                    closeAndDestroyWebView(webViewRef.value, onLog)
                                    onCloseWebView()
                                }
                            }.onFailure { t ->
                                withContext(Dispatchers.Main) {
                                    isRequestInFlight.value = false
                                    if (t is AwaitLoginException) {
                                        onLog("Login not ready, continue waiting. reason=${t.message}")
                                    } else {
                                        onError(t)
                                    }
                                }
                            }
                        }
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        if (request.isForMainFrame) {
                            onError(RuntimeException("WebView error: ${error.errorCode} ${error.description}"))
                        }
                    }
                }

                loadUrl(entryUrl)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun JwxtKcbWebViewTestPagePreview() {
    if (LocalInspectionMode.current) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("JwxtKcbWebView 测试页\n请在真机/模拟器中运行测试登录与课表抓取")
        }
        return
    }

    JwxtKcbWebView(
        modifier = Modifier.fillMaxSize(),
        onKcbJson = {},
        onLog = {},
        onError = {}
    )
}

private data class CurrentUserInfo(
    val xh: String,
    val xnxqdm: String
)

private data class CurrentUserResponse(
    val code: String?,
    val msg: String?,
    val datas: CurrentUserData?
)

private data class CurrentUserData(
    val userId: String?,
    val welcomeInfo: WelcomeInfo?
)

private data class WelcomeInfo(
    val xnxqdm: String?
)

private class AwaitLoginException(message: String) : RuntimeException(message)

private fun fetchCurrentUserInfo(
    client: OkHttpClient,
    cookieHeader: String,
    onLog: (String) -> Unit
): CurrentUserInfo {
    val req = Request.Builder()
        .url("https://jwxt.whut.edu.cn/jwapp/sys/homeapp/api/home/currentUser.do")
        .get()
        .header("Accept", "application/json, text/plain, */*")
        .header("X-Requested-With", "XMLHttpRequest")
        .header("Referer", "https://jwxt.whut.edu.cn/jwapp/sys/homeapp/home/index.html")
        .header("Cookie", cookieHeader)
        .build()

    client.newCall(req).execute().use { resp ->
        val code = readResponseCodeCompat(resp)
        if (code == 301 || code == 302 || code == 401 || code == 403) {
            throw AwaitLoginException("currentUser redirected or unauthorized: HTTP $code")
        }

        val body = readResponseBodyCompat(resp)
        if (!resp.isSuccessful) {
            throw RuntimeException("currentUser HTTP $code: $body")
        }

        val normalized = body.trimStart()
        if (normalized.startsWith("<", ignoreCase = false)) {
            throw AwaitLoginException("currentUser returned HTML, login may be required")
        }

        val root = runCatching {
            Gson().fromJson(body, CurrentUserResponse::class.java)
        }.getOrElse {
            if (it is JsonSyntaxException) {
                throw AwaitLoginException("currentUser JSON not ready")
            }
            throw it
        }

        if (root.code != null && root.code != "0") {
            throw AwaitLoginException("currentUser business code=${root.code}, msg=${root.msg}")
        }

        val xh = root.datas?.userId
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw AwaitLoginException("currentUser 响应中未找到学号字段")

        val xnxqdm = root.datas.welcomeInfo?.xnxqdm
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw AwaitLoginException("currentUser 响应中未找到学年学期字段")

        onLog("currentUser parsed: xh=$xh, xnxqdm=$xnxqdm")
        return CurrentUserInfo(xh = xh, xnxqdm = xnxqdm)
    }
}

private fun clearWebViewCacheOnly(webView: WebView, onLog: (String) -> Unit) {
    runCatching {
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        webView.clearSslPreferences()
        WebStorage.getInstance().deleteAllData()
        onLog("WebView cache/history/storage cleared for fresh start.")
    }.onFailure {
        onLog("Clear WebView cache failed: ${it.message}")
    }
}

private fun fetchKcbDataJson(
    client: OkHttpClient,
    cookieHeader: String,
    xnxqdm: String,
    xh: String
): String {
    val url = "https://jwxt.whut.edu.cn/jwapp/sys/kcbcxby/modules/xskcb/cxxskcb.do"

    val form = FormBody.Builder()
        .add("XNXQDM", xnxqdm)
        .add("XH", xh)
        .build()

    val req = Request.Builder()
        .url(url)
        .post(form)
        // 尽量贴近浏览器请求（通常不是必须，但更稳）
        .header("Accept", "application/json, text/javascript, */*; q=0.01")
        .header("Origin", "https://jwxt.whut.edu.cn")
        .header(
            "Referer",
            "https://jwxt.whut.edu.cn/jwapp/sys/kcbcxby/*default/index.do?THEME=indigo&EMAP_LANG=zh&forceApp=kcbcxby"
        )
        .header("X-Requested-With", "XMLHttpRequest")
        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .header("Cookie", cookieHeader)
        .build()

    client.newCall(req).execute().use { resp ->
        val body = readResponseBodyCompat(resp)
        if (!resp.isSuccessful) {
            throw RuntimeException("HTTP ${readResponseCodeCompat(resp)}: $body")
        }

        val root = runCatching { JsonParser.parseString(body) }
            .getOrElse { throw RuntimeException("课表接口返回非 JSON: $body") }

        if (!root.isJsonObject) {
            throw RuntimeException("课表接口 JSON 结构异常: $body")
        }

        val rootObj = root.asJsonObject
        val code = rootObj.get("code")?.asString
        if (code != null && code != "0") {
            val msg = rootObj.get("msg")?.asString.orEmpty()
            throw RuntimeException("课表接口返回失败: code=$code, msg=$msg")
        }

        val dataElement = rootObj.get("datas") ?: rootObj.get("data")
        if (dataElement == null || dataElement.isJsonNull) {
            throw RuntimeException("课表接口响应中未找到 datas/data 字段")
        }

        return dataElement.toString()
    }
}

private fun closeAndDestroyWebView(webView: WebView?, onLog: (String) -> Unit) {
    if (webView == null) return
    runCatching {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.removeAllViews()
        webView.destroy()
        onLog("WebView closed after timetable data fetched.")
    }.onFailure {
        onLog("Close WebView failed: ${it.message}")
    }
}

private fun readResponseBodyCompat(response: Response): String {
    return runCatching {
        val bodyMethod = response.javaClass.getMethod("body")
        val bodyObj = bodyMethod.invoke(response) ?: return ""
        val stringMethod = bodyObj.javaClass.getMethod("string")
        stringMethod.invoke(bodyObj)?.toString().orEmpty()
    }.getOrDefault("")
}

private fun readResponseCodeCompat(response: Response): Int {
    return runCatching {
        val codeMethod = response.javaClass.getMethod("code")
        (codeMethod.invoke(response) as? Int) ?: -1
    }.getOrDefault(-1)
}
