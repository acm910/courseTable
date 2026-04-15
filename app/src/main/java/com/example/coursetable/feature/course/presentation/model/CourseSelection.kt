package com.example.coursetable.feature.course.presentation.model

import com.example.coursetable.domain.model.CourseSlotVo

sealed interface CourseSelection {
    data class ExistingCourse(val slot: CourseSlotVo) : CourseSelection

    data class EmptySlot(
        val weekDay: Int,
        val periodIndex: Int
    ) : CourseSelection
}


