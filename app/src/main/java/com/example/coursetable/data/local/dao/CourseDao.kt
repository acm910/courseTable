package com.example.coursetable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.coursetable.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Long)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getById(courseId: Long): CourseEntity?

    @Query("SELECT * FROM courses ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<CourseEntity>>
}
