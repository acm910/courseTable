package com.example.coursetable.feature.course.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursetable.core.time.buildSectionTimeLabel
import com.example.coursetable.domain.model.CourseSlotVo

@Composable
fun TodayScheduleScreen(
    selectedWeek: Int,
    weekDay: Int,
    dateText: String,
    courses: List<CourseSlotVo>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "今日课表",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "$dateText · 第${selectedWeek}周 · ${weekDayText(weekDay)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (courses.isEmpty()) {
            Text(
                text = "今天没有课程安排",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(courses, key = { it.sessionId }) { course ->
                TodayCourseCard(course = course)
            }
        }
    }
}

@Composable
private fun TodayCourseCard(course: CourseSlotVo) {
    val endSection = course.startSection + course.sectionCount - 1
    val timeLabel = buildSectionTimeLabel(course.startSection, endSection)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = course.courseName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${course.startSection}-${endSection}节  $timeLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (course.teacher.isNotBlank()) {
                Text(
                    text = "授课教师：${course.teacher}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (course.location.isNotBlank()) {
                Text(
                    text = "授课地点：${course.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}


private fun weekDayText(weekDay: Int): String {
    return when (weekDay) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> "未知"
    }
}

