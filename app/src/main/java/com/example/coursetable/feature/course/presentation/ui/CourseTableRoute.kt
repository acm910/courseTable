package com.example.coursetable.feature.course.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursetable.feature.course.presentation.CourseTableViewModel
import com.example.coursetable.feature.course.presentation.ui.dialog.CourseDialogHost
import com.example.coursetable.feature.webView.JwxtKcbWebView
import kotlinx.coroutines.launch

@Composable
fun CourseTableRoute(
    viewModel: CourseTableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showImportWebView by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.importMessage) {
        val message = uiState.importMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeImportMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        CourseTablePrototype(
            modifier = Modifier.fillMaxSize(),
            previewSemesterStartDate = uiState.semesterStartDate,
            selectedWeek = uiState.selectedWeek,
            showWeekPicker = uiState.showWeekPicker,
            weekCourses = uiState.weekCourses,
            onWeekSelectorClick = viewModel::onWeekSelectorClick,
            onWeekSelected = viewModel::onWeekSelected,
            onWeekPickerDismiss = viewModel::onWeekPickerDismiss,
            onCourseClick = viewModel::onCourseBlockClick,
            onEmptySlotClick = viewModel::onEmptySlotClick
        )

        if (uiState.weekCourses.isEmpty() && !showImportWebView) {
            Text(
                text = "暂无课程，点击右下角 + 一键导入",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
            FloatingActionButton(
                onClick = { showImportWebView = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "导入课程")
            }
        }

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

        if (uiState.isImporting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        CourseDialogHost(
            uiState = uiState,
            onDismiss = viewModel::onDismissDialog,
            onAddFromDetail = viewModel::onAddCourseFromDetail,
            onEditFromDetail = viewModel::onEditCourseFromDetail,
            onDeleteFromDetail = viewModel::onDeleteFromDetail,
            onConfirmDelete = viewModel::onConfirmDeleteCourse,
            onNameChange = viewModel::onNameChange,
            onTeacherChange = viewModel::onTeacherChange,
            onLocationChange = viewModel::onLocationChange,
            onSectionRangeChange = viewModel::onSectionRangeChange,
            onWeekRangeChange = viewModel::onWeekRangeChange,
            onColorChange = viewModel::onColorChange,
            onSubmitForm = viewModel::onSubmitForm
        )
    }
}
