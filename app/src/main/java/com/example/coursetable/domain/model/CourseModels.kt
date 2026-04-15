package com.example.coursetable.domain.model

data class CourseVo(
    val id: Long,
    val name: String,
    val teacher: String,
    val colorIndex: Int
)

data class CourseSlotVo(
    val sessionId: Long,
    val courseId: Long,
    val courseName: String,
    val teacher: String,
    val location: String,
    val weekDay: Int,
    val startSection: Int,
    val sectionCount: Int,
    val weekStart: Int,
    val weekEnd: Int,
    val colorIndex: Int
)

data class CourseDraftVo(
    val name: String,
    val teacher: String,
    val colorIndex: Int
)

data class CourseSessionDraftVo(
    val weekDay: Int,
    val startSection: Int,
    val sectionCount: Int,
    val weekStart: Int,
    val weekEnd: Int,
    val location: String
)

