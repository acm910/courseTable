package com.example.coursetable.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.coursetable.data.local.dao.CourseDao
import com.example.coursetable.data.local.dao.CourseSessionDao
import com.example.coursetable.data.local.entity.CourseEntity
import com.example.coursetable.data.local.entity.CourseSessionEntity

@Database(
    entities = [CourseEntity::class, CourseSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CourseTableDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun courseSessionDao(): CourseSessionDao

    companion object {
        @Volatile
        private var INSTANCE: CourseTableDatabase? = null

        fun getInstance(context: Context): CourseTableDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CourseTableDatabase::class.java,
                    "course_table.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

