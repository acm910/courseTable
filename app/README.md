# courseTable app 模块说明

## 模块概览

`app` 是本项目的 Android 应用模块，使用 **Jetpack Compose + Material 3** 构建界面，采用 `data / domain / feature` 分层组织代码。

- `namespace`: `com.example.coursetable`
- `minSdk`: 33
- `targetSdk`: 36
- `compileSdk`: 36
- Java 版本: 11

## 主要功能

### 1. 课程表页面

- 底部导航第一个页面，默认展示课程表。
- 支持按周查看课程与周数切换。
- 支持课程块点击查看详情。
- 支持新增、编辑、删除课程（弹窗形式）。
- 当本地无课表数据时，显示悬浮 `+` 按钮，可一键导入教务系统课程。

### 2. 今日日程页面

- 底部导航第二个页面，展示当天课程清单。
- 显示课程名、教师、地点。
- 根据节次显示具体时间段（例如 `8:00-8:45`）。

### 3. 设置页面

- 底部导航第三个页面。
- 支持设置学期开始时间。
- 支持删除所有课程。
- 支持重新导入课程（导入前会清空当前课程）。

### 4. 教务系统一键导入

- 通过 `WebView` 完成登录态获取。
- 使用 `OkHttp` 携带 Cookie 请求教务接口。
- 使用 `Gson` 解析用户信息与课表接口数据。
- 导入完成后写入本地数据库。

## 依赖说明

### UI 与 Compose

- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`
- `androidx.compose:compose-bom`
- `androidx.compose.ui:ui`
- `androidx.compose.ui:ui-graphics`
- `androidx.compose.ui:ui-tooling-preview`
- `androidx.compose.material3:material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.compose.material3:material3-adaptive-navigation-suite`
- `androidx.compose.animation:animation`

### 本地存储

- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `androidx.room:room-compiler`（通过 KSP）

### 网络与 JSON

- `com.squareup.okhttp3:okhttp:5.3.2`
- `com.google.code.gson:gson:2.11.0`

### 测试依赖

- `junit:junit`
- `androidx.test.ext:junit`
- `androidx.test.espresso:espresso-core`
- `androidx.compose.ui:ui-test-junit4`
- `androidx.compose.ui:ui-test-manifest`（debug）

## 构建插件

- `com.android.application`
- `org.jetbrains.kotlin.plugin.compose`
- `com.google.devtools.ksp`

## 目录建议（app 模块）

- `src/main/java/com/example/coursetable/core`：基础能力（时间等通用逻辑）
- `src/main/java/com/example/coursetable/data`：数据层（Room、Repository 实现）
- `src/main/java/com/example/coursetable/domain`：领域模型与仓库接口
- `src/main/java/com/example/coursetable/feature`：按功能拆分（course/settings/webView）
- `src/main/java/com/example/coursetable/ui/theme`：主题与样式

## 备注

依赖版本由以下文件统一管理：

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
