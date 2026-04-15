package com.example.coursetable.data.repository

import com.example.coursetable.data.local.dao.CourseDao
import com.example.coursetable.data.local.dao.CourseSessionDao
import com.example.coursetable.data.local.entity.CourseEntity
import com.example.coursetable.data.local.entity.CourseSessionEntity
import com.example.coursetable.data.local.relation.CourseSessionWithCourseRow
import com.example.coursetable.data.repository.impl.CourseRepositoryImpl
import com.example.coursetable.domain.model.CourseDraftVo
import com.example.coursetable.domain.model.CourseSessionDraftVo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CourseRepositoryImplTest {

    private lateinit var fakeCourseDao: FakeCourseDao
    private lateinit var fakeCourseSessionDao: FakeCourseSessionDao
    private lateinit var repository: CourseRepositoryImpl

    @Before
    fun setUp() {
        fakeCourseDao = FakeCourseDao()
        fakeCourseSessionDao = FakeCourseSessionDao(fakeCourseDao)
        repository = CourseRepositoryImpl(fakeCourseDao, fakeCourseSessionDao)
    }

    @Test
    fun addCourseWithSession_andObserveByWeek_returnsExpectedRow() = runBlocking {
        val courseId = repository.addCourseWithSession(
            course = CourseDraftVo(name = "高等数学", teacher = "张老师", colorIndex = 3),
            session = CourseSessionDraftVo(
                weekDay = 1,
                startSection = 1,
                sectionCount = 2,
                weekStart = 1,
                weekEnd = 20,
                location = "A101"
            )
        )

        assertTrue(courseId > 0)

        val week1 = repository.observeWeekCourses(1).first()
        val week21 = repository.observeWeekCourses(21).first()

        assertEquals(1, week1.size)
        assertEquals(0, week21.size)
        assertEquals("高等数学", week1.first().courseName)
        assertEquals("张老师", week1.first().teacher)
        assertEquals("A101", week1.first().location)
        assertEquals(3, week1.first().colorIndex)
    }

    @Test
    fun updateCourseAndSession_observeReflectsChanges() = runBlocking {
        val courseId = repository.addCourseWithSession(
            course = CourseDraftVo(name = "英语", teacher = "李老师", colorIndex = 1),
            session = CourseSessionDraftVo(
                weekDay = 2,
                startSection = 3,
                sectionCount = 3,
                weekStart = 1,
                weekEnd = 16,
                location = "B201"
            )
        )

        val oldSession = repository.observeWeekCourses(4).first().first()

        repository.updateCourse(
            courseId = courseId,
            course = CourseDraftVo(name = "大学英语", teacher = "王老师", colorIndex = 7)
        )
        repository.updateSession(
            sessionId = oldSession.sessionId,
            courseId = courseId,
            session = CourseSessionDraftVo(
                weekDay = 4,
                startSection = 4,
                sectionCount = 2,
                weekStart = 2,
                weekEnd = 18,
                location = "C303"
            )
        )

        val week4 = repository.observeWeekCourses(4).first()
        val updated = week4.first()

        assertEquals("大学英语", updated.courseName)
        assertEquals("王老师", updated.teacher)
        assertEquals(7, updated.colorIndex)
        assertEquals(4, updated.weekDay)
        assertEquals(4, updated.startSection)
        assertEquals(2, updated.sectionCount)
        assertEquals("C303", updated.location)
    }

    @Test
    fun deleteCourse_removesRelatedSessions() = runBlocking {
        val courseId = repository.addCourseWithSession(
            course = CourseDraftVo(name = "数据结构", teacher = "周老师", colorIndex = 5),
            session = CourseSessionDraftVo(
                weekDay = 5,
                startSection = 6,
                sectionCount = 2,
                weekStart = 1,
                weekEnd = 12,
                location = "D402"
            )
        )

        assertEquals(1, repository.observeWeekCourses(6).first().size)

        repository.deleteCourse(courseId)

        assertEquals(0, repository.observeWeekCourses(6).first().size)
    }
}

private class FakeCourseDao : CourseDao {
    private val courses = linkedMapOf<Long, CourseEntity>()
    private val courseFlow = MutableStateFlow<List<CourseEntity>>(emptyList())
    private var idSeed = 1L

    var onCourseChanged: (() -> Unit)? = null
    var onCourseDeleted: ((Long) -> Unit)? = null

    override suspend fun insert(course: CourseEntity): Long {
        val id = if (course.id == 0L) idSeed++ else course.id
        courses[id] = course.copy(id = id)
        publish()
        return id
    }

    override suspend fun update(course: CourseEntity) {
        if (courses.containsKey(course.id)) {
            courses[course.id] = course
            publish()
        }
    }

    override suspend fun deleteById(courseId: Long) {
        if (courses.remove(courseId) != null) {
            onCourseDeleted?.invoke(courseId)
            publish()
        }
    }

    override suspend fun getById(courseId: Long): CourseEntity? = courses[courseId]

    override fun observeAll(): Flow<List<CourseEntity>> = courseFlow

    fun allCourses(): List<CourseEntity> = courses.values.toList()

    private fun publish() {
        courseFlow.value = courses.values.sortedByDescending { it.updatedAt }
        onCourseChanged?.invoke()
    }
}

private class FakeCourseSessionDao(
    private val courseDao: FakeCourseDao
) : CourseSessionDao {
    private val sessions = linkedMapOf<Long, CourseSessionEntity>()
    private val joinedRows = MutableStateFlow<List<CourseSessionWithCourseRow>>(emptyList())
    private var idSeed = 1L

    init {
        courseDao.onCourseChanged = { refreshRows() }
        courseDao.onCourseDeleted = { deletedCourseId ->
            sessions.entries.removeAll { it.value.courseId == deletedCourseId }
            refreshRows()
        }
    }

    override suspend fun insert(session: CourseSessionEntity): Long {
        val id = if (session.id == 0L) idSeed++ else session.id
        sessions[id] = session.copy(id = id)
        refreshRows()
        return id
    }

    override suspend fun update(session: CourseSessionEntity) {
        if (sessions.containsKey(session.id)) {
            sessions[session.id] = session
            refreshRows()
        }
    }

    override suspend fun deleteByCourseId(courseId: Long) {
        sessions.entries.removeAll { it.value.courseId == courseId }
        refreshRows()
    }

    override suspend fun deleteById(sessionId: Long) {
        sessions.remove(sessionId)
        refreshRows()
    }

    override fun observeByWeek(selectedWeek: Int): Flow<List<CourseSessionWithCourseRow>> {
        val week = selectedWeek.coerceAtLeast(1)
        return joinedRows.map { rows ->
            rows.filter { week in it.weekStart..it.weekEnd }
                .sortedWith(compareBy({ it.weekDay }, { it.startSection }))
        }
    }

    private fun refreshRows() {
        val coursesById = courseDao.allCourses().associateBy { it.id }
        joinedRows.value = sessions.values.mapNotNull { session ->
            val course = coursesById[session.courseId] ?: return@mapNotNull null
            CourseSessionWithCourseRow(
                sessionId = session.id,
                courseId = course.id,
                courseName = course.name,
                teacher = course.teacher,
                colorIndex = course.colorIndex,
                location = session.location,
                weekDay = session.weekDay,
                startSection = session.startSection,
                sectionCount = session.sectionCount,
                weekStart = session.weekStart,
                weekEnd = session.weekEnd
            )
        }
    }
}

