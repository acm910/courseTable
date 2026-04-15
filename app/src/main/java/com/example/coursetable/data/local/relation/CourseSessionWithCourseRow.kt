package com.example.coursetable.data.local.relation

import androidx.room.ColumnInfo

data class CourseSessionWithCourseRow(
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "course_name")
    val courseName: String,
    @ColumnInfo(name = "teacher")
    val teacher: String,
    @ColumnInfo(name = "color_index")
    val colorIndex: Int,
    @ColumnInfo(name = "location")
    val location: String,
    @ColumnInfo(name = "week_day")
    val weekDay: Int,
    @ColumnInfo(name = "start_section")
    val startSection: Int,
    @ColumnInfo(name = "section_count")
    val sectionCount: Int,
    @ColumnInfo(name = "week_start")
    val weekStart: Int,
    @ColumnInfo(name = "week_end")
    val weekEnd: Int
)


