package com.example.coursetable.core.time

data class CourseSectionTime(val start: String, val end: String)

private val sectionTimeMap = mapOf(
    1 to CourseSectionTime("8:00", "8:45"),
    2 to CourseSectionTime("8:50", "9:35"),
    3 to CourseSectionTime("9:55", "10:40"),
    4 to CourseSectionTime("10:45", "11:30"),
    5 to CourseSectionTime("11:35", "12:20"),
    6 to CourseSectionTime("14:00", "14:45"),
    7 to CourseSectionTime("14:50", "15:35"),
    8 to CourseSectionTime("15:40", "16:25"),
    9 to CourseSectionTime("16:45", "17:30"),
    10 to CourseSectionTime("17:35", "18:20"),
    11 to CourseSectionTime("19:00", "19:40"),
    12 to CourseSectionTime("19:45", "20:30"),
    13 to CourseSectionTime("20:35", "21:20")
)

fun buildSectionTimeLabel(startSection: Int, endSection: Int): String {
    val start = sectionTimeMap[startSection]?.start
    val end = sectionTimeMap[endSection]?.end
    return if (start != null && end != null) "$start-$end" else "时间待定"
}

