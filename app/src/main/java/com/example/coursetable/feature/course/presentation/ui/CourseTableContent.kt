package com.example.coursetable.feature.course.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.coursetable.domain.model.CourseSlotVo

@Composable
fun CourseTableContent(
    selectedWeek: Int,
    showWeekPicker: Boolean,
    weekCourses: List<CourseSlotVo>,
    onWeekSelectorClick: () -> Unit,
    onWeekSelected: (Int) -> Unit,
    onWeekPickerDismiss: () -> Unit,
    onCourseClick: (CourseSlotVo) -> Unit,
    onEmptySlotClick: (weekDay: Int, periodIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CourseTablePrototype(
        selectedWeek = selectedWeek,
        showWeekPicker = showWeekPicker,
        weekCourses = weekCourses,
        onWeekSelectorClick = onWeekSelectorClick,
        onWeekSelected = onWeekSelected,
        onWeekPickerDismiss = onWeekPickerDismiss,
        onCourseClick = onCourseClick,
        onEmptySlotClick = onEmptySlotClick,
        modifier = modifier
    )
}


