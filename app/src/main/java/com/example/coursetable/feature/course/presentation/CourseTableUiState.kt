package com.example.coursetable.feature.course.presentation

import com.example.coursetable.domain.model.CourseSlotVo
import com.example.coursetable.feature.course.presentation.model.CourseDialogState
import com.example.coursetable.feature.course.presentation.model.CourseFormState
import com.example.coursetable.feature.course.presentation.model.CourseSelection

data class CourseTableUiState(
    val selectedWeek: Int = 1,
    val showWeekPicker: Boolean = false,
    val weekCourses: List<CourseSlotVo> = emptyList(),
    val dialogState: CourseDialogState = CourseDialogState.None,
    val formState: CourseFormState = CourseFormState(),
    val activeSelection: CourseSelection? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false
)


