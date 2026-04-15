package com.example.coursetable.presentation.course.ui.dialog

import androidx.compose.runtime.Composable
import com.example.coursetable.presentation.course.CourseTableUiState
import com.example.coursetable.presentation.course.model.CourseDialogState

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
    onWeekDayChange: (Int) -> Unit,
    onStartSectionChange: (Int) -> Unit,
    onSectionCountChange: (Int) -> Unit,
    onWeekStartChange: (Int) -> Unit,
    onWeekEndChange: (Int) -> Unit,
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
                onWeekDayChange = onWeekDayChange,
                onStartSectionChange = onStartSectionChange,
                onSectionCountChange = onSectionCountChange,
                onWeekStartChange = onWeekStartChange,
                onWeekEndChange = onWeekEndChange,
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


