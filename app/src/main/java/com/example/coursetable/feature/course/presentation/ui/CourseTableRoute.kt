package com.example.coursetable.feature.course.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.coursetable.feature.course.presentation.CourseTableViewModel
import com.example.coursetable.feature.course.presentation.ui.dialog.CourseDialogHost

@Composable
fun CourseTableRoute(
    viewModel: CourseTableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        CourseTablePrototype(
            modifier = Modifier.fillMaxSize(),
            selectedWeek = uiState.selectedWeek,
            showWeekPicker = uiState.showWeekPicker,
            weekCourses = uiState.weekCourses,
            onWeekSelectorClick = viewModel::onWeekSelectorClick,
            onWeekSelected = viewModel::onWeekSelected,
            onWeekPickerDismiss = viewModel::onWeekPickerDismiss,
            onCourseClick = viewModel::onCourseBlockClick,
            onEmptySlotClick = viewModel::onEmptySlotClick,
            showDebugText = true
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



