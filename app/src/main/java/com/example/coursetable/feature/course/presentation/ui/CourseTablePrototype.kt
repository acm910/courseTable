package com.example.coursetable.feature.course.presentation.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coursetable.core.time.SemesterStartDateStore
import com.example.coursetable.domain.model.CourseSlotVo
import com.example.coursetable.feature.course.presentation.ui.table.DayColumn
import com.example.coursetable.feature.course.presentation.ui.table.TimePeriodColumn
import com.example.coursetable.feature.course.presentation.ui.table.WeekPickerBottomSheet
import com.example.coursetable.feature.course.presentation.ui.table.WeekSelector
import com.example.coursetable.feature.course.presentation.ui.table.buildWeekSchedules
import com.example.coursetable.feature.course.presentation.ui.table.calculateDayWidth
import com.example.coursetable.feature.course.presentation.ui.table.calculateSlotHeights
import com.example.coursetable.feature.course.presentation.ui.table.calculateVisibleDays
import com.example.coursetable.feature.course.presentation.ui.table.slotWeights
import java.util.Calendar
import java.time.LocalDate

@Preview
@Composable
fun CourseTablePreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CourseTablePrototype(
                previewSemesterStartDate = LocalDate.of(2026, 4, 13),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun CourseTablePrototype(
    previewSemesterStartDate: LocalDate? = null,
    selectedWeek: Int = 1,
    showWeekPicker: Boolean = false,
    weekCourses: List<CourseSlotVo> = emptyList(),
    onWeekSelectorClick: (() -> Unit)? = null,
    onWeekSelected: ((Int) -> Unit)? = null,
    onWeekPickerDismiss: (() -> Unit)? = null,
    onCourseClick: ((CourseSlotVo) -> Unit)? = null,
    onEmptySlotClick: ((weekDay: Int, periodIndex: Int) -> Unit)? = null,
    showDebugText: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val semesterStartDate = previewSemesterStartDate ?: remember { SemesterStartDateStore.get(context) }

    var localSelectedWeek by remember { mutableStateOf(1) }
    var localShowWeekPicker by remember { mutableStateOf(false) }
    val effectiveSelectedWeek = if (onWeekSelected != null) selectedWeek else localSelectedWeek
    val effectiveShowWeekPicker = if (onWeekSelectorClick != null && onWeekPickerDismiss != null) showWeekPicker else localShowWeekPicker
    val schedules = remember(semesterStartDate, effectiveSelectedWeek) {
        buildWeekSchedules(semesterStartDate, effectiveSelectedWeek)
    }

    val weekSelectorHeight = 45.dp
    val leftTimeLabelWidth = 12.dp
    val minCourseBlockWidth = 65.dp
    val daySpacing = 8.dp
    val blockSpacing = 8.dp
    val dayHeaderHeight = 48.dp
    val headerBottomGap = 6.dp
    val today = remember { Calendar.getInstance() }
    val todayMonth = today.get(Calendar.MONTH) + 1
    val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = (maxWidth - leftTimeLabelWidth - daySpacing).coerceAtLeast(0.dp)
        val visibleDays = calculateVisibleDays(contentWidth, minCourseBlockWidth, daySpacing)
        val dayWidth = calculateDayWidth(contentWidth, visibleDays, daySpacing)

        val tableBodyHeight = (maxHeight - weekSelectorHeight - 8.dp).coerceAtLeast(0.dp)
        val slotAreaHeight = (tableBodyHeight - dayHeaderHeight - headerBottomGap).coerceAtLeast(0.dp)
        val slotHeights = calculateSlotHeights(slotAreaHeight, blockSpacing, slotWeights)

        Column(modifier = Modifier.fillMaxSize()) {
            WeekSelector(
                selectedWeek = effectiveSelectedWeek,
                isExpanded = effectiveShowWeekPicker,
                onClick = {
                    onWeekSelectorClick?.invoke() ?: run { localShowWeekPicker = true }
                },
                modifier = Modifier
                    .height(weekSelectorHeight)
                    .fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .height(tableBodyHeight)
                    .fillMaxWidth()
            ) {
                TimePeriodColumn(
                    month = schedules.firstOrNull()?.month ?: selectedWeek,
                    slotHeights = slotHeights,
                    blockSpacing = blockSpacing,
                    dayHeaderHeight = dayHeaderHeight,
                    headerBottomGap = headerBottomGap,
                    modifier = Modifier
                        .width(leftTimeLabelWidth)
                        .fillMaxHeight()
                )

                Spacer(modifier = Modifier.width(daySpacing))

                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                ) {
                    schedules.forEachIndexed { index, day ->
                        DayColumn(
                            dayOfMonth = day.dayOfMonth,
                            weekDay = day.weekDay,
                            isToday = day.month == todayMonth && day.dayOfMonth == todayDayOfMonth,
                            weekDayIndex = index + 1,
                            dayWidth = dayWidth,
                            slotHeights = slotHeights,
                            blockSpacing = blockSpacing,
                            dayHeaderHeight = dayHeaderHeight,
                            headerBottomGap = headerBottomGap,
                            selectedWeek = effectiveSelectedWeek,
                            weekCourses = weekCourses,
                            onCourseClick = onCourseClick,
                            onEmptySlotClick = onEmptySlotClick,
                            showDebugText = showDebugText
                        )
                        if (index != schedules.lastIndex) {
                            Spacer(modifier = Modifier.width(daySpacing))
                        }
                    }
                }
            }
        }

        if (effectiveShowWeekPicker) {
            WeekPickerBottomSheet(
                selectedWeek = effectiveSelectedWeek,
                onWeekSelected = {
                    onWeekSelected?.invoke(it) ?: run { localSelectedWeek = it }
                    if (onWeekPickerDismiss == null) {
                        localShowWeekPicker = false
                    }
                },
                onDismiss = {
                    onWeekPickerDismiss?.invoke() ?: run { localShowWeekPicker = false }
                }
            )
        }
    }
}
