package com.example.coursetable.feature.course.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.coursetable.feature.course.presentation.CourseTableViewModel
import com.example.coursetable.feature.course.presentation.ui.table.calculateCurrentWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScheduleRoute(
    viewModel: CourseTableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val todayWeekDay = today.dayOfWeek.value
    val dateText = today.format(DateTimeFormatter.ofPattern("MM-dd"))
    val todayWeek = calculateCurrentWeek(
        semesterStartDate = uiState.semesterStartDate,
        today = today
    )

    TodayScheduleScreen(
        modifier = modifier,
        selectedWeek = todayWeek,
        weekDay = todayWeekDay,
        dateText = dateText,
        courses = uiState.weekCourses
            .filter { slot ->
                slot.weekDay == todayWeekDay && todayWeek in slot.weekStart..slot.weekEnd
            }
            .sortedBy { it.startSection }
    )
}

