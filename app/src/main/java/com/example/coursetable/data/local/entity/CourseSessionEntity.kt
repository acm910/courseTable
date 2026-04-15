package com.example.coursetable.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "course_sessions",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["course_id"]),
        Index(value = ["week_day"]),
        Index(value = ["week_start"]),
        Index(value = ["week_end"])
    ]
)
data class CourseSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "week_day")
    val weekDay: Int,
    @ColumnInfo(name = "start_section")
    val startSection: Int,
    @ColumnInfo(name = "section_count")
    val sectionCount: Int,
    @ColumnInfo(name = "week_start")
    val weekStart: Int,
    @ColumnInfo(name = "week_end")
    val weekEnd: Int,
    @ColumnInfo(name = "location")
    val location: String
)

