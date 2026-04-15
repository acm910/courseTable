package com.example.coursetable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.coursetable.data.local.entity.CourseSessionEntity
import com.example.coursetable.data.local.model.CourseSessionWithCourseRow
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: CourseSessionEntity): Long

    @Update
    suspend fun update(session: CourseSessionEntity)

    @Query("DELETE FROM course_sessions WHERE course_id = :courseId")
    suspend fun deleteByCourseId(courseId: Long)

    @Query("DELETE FROM course_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Long)

    @Query(
        """
        SELECT
            cs.id AS session_id,
            c.id AS course_id,
            c.name AS course_name,
            c.teacher AS teacher,
            c.color_index AS color_index,
            cs.location AS location,
            cs.week_day AS week_day,
            cs.start_section AS start_section,
            cs.section_count AS section_count,
            cs.week_start AS week_start,
            cs.week_end AS week_end
        FROM course_sessions cs
        INNER JOIN courses c ON c.id = cs.course_id
        WHERE :selectedWeek BETWEEN cs.week_start AND cs.week_end
        ORDER BY cs.week_day ASC, cs.start_section ASC
        """
    )
    fun observeByWeek(selectedWeek: Int): Flow<List<CourseSessionWithCourseRow>>
}

