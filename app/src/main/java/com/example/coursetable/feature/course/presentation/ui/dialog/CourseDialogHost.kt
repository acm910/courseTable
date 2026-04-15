package com.example.coursetable.feature.course.presentation.ui.dialog

import androidx.compose.runtime.Composable
import com.example.coursetable.feature.course.presentation.CourseTableUiState
import com.example.coursetable.feature.course.presentation.model.CourseDialogState

@Composable
fun CourseDialogHost(
    uiState: CourseTableUiState,
    onDismiss: () -> Unit,
    onAddFromDetail: () -> Unit,
    onEditFromDetail: () -> Unit,
    onDeleteFromDetail: () -> Unit,
    onConfirmDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    onTeacherChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSectionRangeChange: (startSection: Int, sectionCount: Int) -> Unit,
    onWeekRangeChange: (start: Int, end: Int) -> Unit,
    onColorChange: (Int) -> Unit,
    onSubmitForm: () -> Unit
) {
    when (val state = uiState.dialogState) {
        is CourseDialogState.None -> Unit
        is CourseDialogState.Detail -> {
            CourseDetailDialog(
                slot = state.slot,
                onDismiss = onDismiss,
                onAddCourse = onAddFromDetail,
                onEditCourse = onEditFromDetail,
                onDeleteCourse = onDeleteFromDetail
            )
        }

        is CourseDialogState.Form -> {
            CourseFormDialog(
                mode = state.mode,
                formState = uiState.formState,
                isSaving = uiState.isSaving,
                onDismiss = onDismiss,
                onNameChange = onNameChange,
                onTeacherChange = onTeacherChange,
                onLocationChange = onLocationChange,
                onSectionRangeChange = onSectionRangeChange,
                onWeekRangeChange = onWeekRangeChange,
                onColorChange = onColorChange,
                onSubmit = onSubmitForm
            )
        }

        is CourseDialogState.DeleteConfirm -> {
            DeleteCourseConfirmDialog(
                courseName = state.slot.courseName,
                isDeleting = uiState.isDeleting,
                onDismiss = {
                    if (!uiState.isDeleting) onDismiss()
                },
                onConfirmDelete = onConfirmDelete
            )
        }
    }
}



