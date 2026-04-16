package com.example.coursetable.domain.model

data class CourseImportItemVo(
    val courseName: String,
    val teacher: String,
    val location: String,
    val weekDay: Int,
    val startSection: Int,
    val sectionCount: Int,
    val weekStart: Int,
    val weekEnd: Int
)

data class CourseImportResultVo(
    val importedCourseCount: Int,
    val importedSessionCount: Int,
    val skippedCount: Int
)

