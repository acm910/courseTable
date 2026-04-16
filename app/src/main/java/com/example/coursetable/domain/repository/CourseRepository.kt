package com.example.coursetable.domain.repository

import com.example.coursetable.domain.model.CourseDraftVo
import com.example.coursetable.domain.model.CourseImportItemVo
import com.example.coursetable.domain.model.CourseImportResultVo
import com.example.coursetable.domain.model.CourseSessionDraftVo
import com.example.coursetable.domain.model.CourseSlotVo
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeWeekCourses(selectedWeek: Int): Flow<List<CourseSlotVo>>

    suspend fun addCourseWithSession(
        course: CourseDraftVo,
        session: CourseSessionDraftVo
    ): Long

    suspend fun updateCourse(
        courseId: Long,
        course: CourseDraftVo
    )

    suspend fun updateSession(
        sessionId: Long,
        courseId: Long,
        session: CourseSessionDraftVo
    )

    suspend fun deleteCourse(courseId: Long)

    suspend fun clearAllCourses()

    suspend fun replaceAllCourses(importItems: List<CourseImportItemVo>): CourseImportResultVo
}
