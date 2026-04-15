package com.example.coursetable.presentation.course.model

import com.example.coursetable.domain.model.CourseSlotVo

sealed interface CourseDialogState {
    data object None : CourseDialogState
    data class Detail(val slot: CourseSlotVo) : CourseDialogState
    data class Form(val mode: CourseFormMode) : CourseDialogState
    data class DeleteConfirm(val slot: CourseSlotVo) : CourseDialogState
}

enum class CourseFormMode {
    Create,
    Edit
}

