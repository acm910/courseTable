package com.example.coursetable.presentation.course.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.coursetable.CourseTablePrototype
import com.example.coursetable.presentation.course.CourseTableViewModel
import com.example.coursetable.presentation.course.model.CourseDialogState
import com.example.coursetable.presentation.course.ui.dialog.CourseDialogHost

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
            onEmptySlotClick = viewModel::onEmptySlotClick
        )

        if (uiState.dialogState !is CourseDialogState.None) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

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
            onWeekDayChange = viewModel::onWeekDayChange,
            onStartSectionChange = viewModel::onStartSectionChange,
            onSectionCountChange = viewModel::onSectionCountChange,
            onWeekStartChange = viewModel::onWeekStartChange,
            onWeekEndChange = viewModel::onWeekEndChange,
            onColorChange = viewModel::onColorChange,
            onSubmitForm = viewModel::onSubmitForm
        )
    }
}


