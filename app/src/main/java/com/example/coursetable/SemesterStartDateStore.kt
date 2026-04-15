package com.example.coursetable

import android.content.Context
import java.time.LocalDate

/**
 * Global semester start date holder with SharedPreferences persistence.
 */
object SemesterStartDateStore {
    private const val PREFS_NAME = "course_table_prefs"
    private const val KEY_SEMESTER_START_EPOCH_DAY = "semester_start_epoch_day"

    @Volatile
    private var cachedStartDate: LocalDate? = null

    fun get(context: Context): LocalDate {
        cachedStartDate?.let { return it }
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultEpochDay = LocalDate.now().toEpochDay()
        val epochDay = prefs.getLong(KEY_SEMESTER_START_EPOCH_DAY, defaultEpochDay)
        return LocalDate.ofEpochDay(epochDay).also { cachedStartDate = it }
    }

    fun set(context: Context, startDate: LocalDate) {
        cachedStartDate = startDate
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_SEMESTER_START_EPOCH_DAY, startDate.toEpochDay()).apply()
    }
}

