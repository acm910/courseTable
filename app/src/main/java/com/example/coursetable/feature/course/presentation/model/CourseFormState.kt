package com.example.coursetable.feature.course.presentation.model

data class CourseFormState(
	val courseId: Long? = null,
	val sessionId: Long? = null,
	val name: String = "",
	val teacher: String = "",
	val location: String = "",
	val weekDay: Int = 1,
	val periodIndex: Int = 0,
	val startSection: Int = 1,
	val sectionCount: Int = 2,
	val weekStart: Int = 1,
	val weekEnd: Int = 1,
	val colorIndex: Int = 0,
	val errorMessage: String? = null
)


