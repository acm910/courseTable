package com.example.coursetable.data.repository

import android.content.Context
import com.example.coursetable.data.local.CourseTableDatabase

object CourseRepositoryProvider {
    @Volatile
    private var instance: CourseRepository? = null

    fun get(context: Context): CourseRepository {
        return instance ?: synchronized(this) {
            val db = CourseTableDatabase.getInstance(context.applicationContext)
            val repository = CourseRepositoryImpl(
                courseDao = db.courseDao(),
                courseSessionDao = db.courseSessionDao()
            )
            instance = repository
            repository
        }
    }
}

