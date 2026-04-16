package com.example.coursetable.feature.settings.presentation.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.coursetable.feature.course.presentation.CourseTableViewModel
import com.example.coursetable.feature.webView.JwxtKcbWebView
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun SettingsRoute(
    viewModel: CourseTableViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showReimportConfirm by remember { mutableStateOf(false) }
    var showImportWebView by remember { mutableStateOf(false) }
    var isWaitingReimportClear by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.importMessage) {
        val message = uiState.importMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeImportMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        val startDateText = remember(uiState.semesterStartDate) {
            uiState.semesterStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }

        SettingsScreen(
            semesterStartDateText = startDateText,
            onSemesterStartDateClick = {
                val current = uiState.semesterStartDate
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        viewModel.onSemesterStartDateChange(
                            java.time.LocalDate.of(year, month + 1, dayOfMonth)
                        )
                    },
                    current.year,
                    current.monthValue - 1,
                    current.dayOfMonth
                ).show()
            },
            onDeleteAllClick = { showDeleteConfirm = true },
            onReimportClick = { showReimportConfirm = true },
            modifier = Modifier.fillMaxSize()
        )

        if (showImportWebView) {
            JwxtKcbWebView(
                modifier = Modifier.fillMaxSize(),
                onKcbJson = { viewModel.importCoursesFromWebPayload(it) },
                onCloseWebView = { showImportWebView = false },
                onError = {
                    showImportWebView = false
                    scope.launch {
                        snackbarHostState.showSnackbar("导入页面加载失败，请重试")
                    }
                }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("删除全部课程") },
                text = { Text("确认删除当前全部课程吗？该操作不可恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            viewModel.clearAllCourses()
                        }
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showReimportConfirm) {
            AlertDialog(
                onDismissRequest = { showReimportConfirm = false },
                title = { Text("重新导入课程") },
                text = { Text("将先删除当前课程，再打开教务系统导入页面，是否继续？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showReimportConfirm = false
                            isWaitingReimportClear = true
                            viewModel.clearAllCourses { success ->
                                isWaitingReimportClear = false
                                if (success) {
                                    showImportWebView = true
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("清空课程失败，未进入导入页面")
                                    }
                                }
                            }
                        }
                    ) {
                        Text("继续")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReimportConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (uiState.isImporting || isWaitingReimportClear) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}


