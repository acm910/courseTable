package com.example.coursetable.data.repository.impl

import com.example.coursetable.data.local.dao.CourseDao
import com.example.coursetable.data.local.dao.CourseSessionDao
import com.example.coursetable.data.local.entity.CourseEntity
import com.example.coursetable.data.local.entity.CourseSessionEntity
import com.example.coursetable.domain.model.CourseDraftVo
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
}


