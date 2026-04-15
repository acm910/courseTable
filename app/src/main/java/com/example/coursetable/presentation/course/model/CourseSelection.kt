package com.example.coursetable.presentation.course.model

import com.example.coursetable.domain.model.CourseSlotVo

sealed interface CourseSelection {
    data class ExistingCourse(val slot: CourseSlotVo) : CourseSelection

    data class EmptySlot(
        val weekDay: Int,
        val periodIndex: Int,
        val startSection: Int,
        val defaultSectionCount: Int
    ) : CourseSelection
}

