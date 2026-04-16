package com.example.coursetable.data.repository.impl

import com.example.coursetable.data.local.dao.CourseDao
import com.example.coursetable.data.local.dao.CourseSessionDao
import com.example.coursetable.data.local.entity.CourseEntity
import com.example.coursetable.data.local.entity.CourseSessionEntity
import com.example.coursetable.domain.model.CourseColorPalette
import com.example.coursetable.domain.model.CourseDraftVo
import com.example.coursetable.domain.model.CourseImportItemVo
import com.example.coursetable.domain.model.CourseImportResultVo
import com.example.coursetable.domain.model.CourseSessionDraftVo
import com.example.coursetable.domain.model.CourseSlotVo
import com.example.coursetable.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
    private val courseSessionDao: CourseSessionDao
) : CourseRepository {

    override fun observeWeekCourses(selectedWeek: Int): Flow<List<CourseSlotVo>> {
        return courseSessionDao.observeAll().map { rows ->
            rows.map { row ->
                CourseSlotVo(
                    sessionId = row.sessionId,
                    courseId = row.courseId,
                    courseName = row.courseName,
                    teacher = row.teacher,
                    location = row.location,
                    weekDay = row.weekDay,
                    startSection = row.startSection,
                    sectionCount = row.sectionCount,
                    weekStart = row.weekStart,
                    weekEnd = row.weekEnd,
                    colorIndex = row.colorIndex
                )
            }
        }
    }

    override suspend fun addCourseWithSession(course: CourseDraftVo, session: CourseSessionDraftVo): Long {
        val now = System.currentTimeMillis()
        val courseId = courseDao.insert(
            CourseEntity(
                name = course.name,
                teacher = course.teacher,
                colorIndex = course.colorIndex,
                createdAt = now,
                updatedAt = now
            )
        )
        courseSessionDao.insert(
            CourseSessionEntity(
                courseId = courseId,
                weekDay = session.weekDay,
                startSection = session.startSection,
                sectionCount = session.sectionCount,
                weekStart = session.weekStart,
                weekEnd = session.weekEnd,
                location = session.location
            )
        )
        return courseId
    }

    override suspend fun updateCourse(courseId: Long, course: CourseDraftVo) {
        val existing = courseDao.getById(courseId) ?: return
        courseDao.update(
            existing.copy(
                name = course.name,
                teacher = course.teacher,
                colorIndex = course.colorIndex,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateSession(sessionId: Long, courseId: Long, session: CourseSessionDraftVo) {
        courseSessionDao.update(
            CourseSessionEntity(
                id = sessionId,
                courseId = courseId,
                weekDay = session.weekDay,
                startSection = session.startSection,
                sectionCount = session.sectionCount,
                weekStart = session.weekStart,
                weekEnd = session.weekEnd,
                location = session.location
            )
        )
    }

    override suspend fun deleteCourse(courseId: Long) {
        // Cascading foreign key removes all related sessions.
        courseDao.deleteById(courseId)
    }

    override suspend fun clearAllCourses() {
        // Cascading foreign key removes all related sessions.
        courseDao.deleteAll()
    }

    override suspend fun replaceAllCourses(importItems: List<CourseImportItemVo>): CourseImportResultVo {
        clearAllCourses()

        val normalizedItems = importItems.mapNotNull { item ->
            if (item.courseName.isBlank()) {
                null
            } else {
                item.copy(
                    weekDay = item.weekDay.coerceIn(1, 7),
                    startSection = item.startSection.coerceAtLeast(1),
                    sectionCount = item.sectionCount.coerceAtLeast(1),
                    weekStart = item.weekStart.coerceIn(1, 20),
                    weekEnd = item.weekEnd.coerceIn(1, 20)
                )
            }
        }

        val grouped = normalizedItems.groupBy { keyOf(it.courseName, it.teacher) }
        var importedCourses = 0
        var importedSessions = 0
        val now = System.currentTimeMillis()

        grouped.forEach { (_, sessions) ->
            val sample = sessions.first()
            val courseId = courseDao.insert(
                CourseEntity(
                    name = sample.courseName,
                    teacher = sample.teacher,
                    colorIndex = colorIndexFor(sample.courseName, sample.teacher),
                    createdAt = now,
                    updatedAt = now
                )
            )
            importedCourses++

            sessions.forEach { session ->
                val normalizedStart = minOf(session.weekStart, session.weekEnd)
                val normalizedEnd = maxOf(session.weekStart, session.weekEnd)
                courseSessionDao.insert(
                    CourseSessionEntity(
                        courseId = courseId,
                        weekDay = session.weekDay,
                        startSection = session.startSection,
                        sectionCount = session.sectionCount,
                        weekStart = normalizedStart,
                        weekEnd = normalizedEnd,
                        location = session.location
                    )
                )
                importedSessions++
            }
        }

        return CourseImportResultVo(
            importedCourseCount = importedCourses,
            importedSessionCount = importedSessions,
            skippedCount = importItems.size - normalizedItems.size
        )
    }

    private fun keyOf(courseName: String, teacher: String): String {
        return "${courseName.trim()}::${teacher.trim()}"
    }

    private fun colorIndexFor(courseName: String, teacher: String): Int {
        val key = keyOf(courseName, teacher)
        val paletteSize = CourseColorPalette.presetColors.size
        return key.hashCode().mod(paletteSize)
    }
}
