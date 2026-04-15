package com.example.coursetable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.domain.model.CourseColorPalette
import com.example.coursetable.domain.model.CourseSlotVo
import java.util.Calendar
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.max

private val slotWeights = listOf(2f, 3f, 3f, 2f, 3f)

@Preview()
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
    onEmptySlotClick: ((weekDay: Int, periodIndex: Int, startSection: Int, defaultSectionCount: Int) -> Unit)? = null,
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
                modifier = Modifier.height(weekSelectorHeight).fillMaxWidth()
            )


            Row(modifier = Modifier.height(tableBodyHeight).fillMaxWidth()) {
                TimePeriodColumn(
                    month = schedules.firstOrNull()?.month ?: selectedWeek,
                    slotHeights = slotHeights,
                    blockSpacing = blockSpacing,
                    dayHeaderHeight = dayHeaderHeight,
                    headerBottomGap = headerBottomGap,
                    modifier = Modifier.width(leftTimeLabelWidth).fillMaxHeight()
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
                            weekCourses = weekCourses,
                            onCourseClick = onCourseClick,
                            onEmptySlotClick = onEmptySlotClick
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekSelector(
    selectedWeek: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f, label = "week_selector_arrow")

    Box(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "第 ${selectedWeek} 周",
                textAlign = TextAlign.Center,
                color = Color(0xFF1D2939)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = android.R.drawable.arrow_down_float),
                contentDescription = "选择周数",
                tint = Color(0xFF1D2939),
                modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerBottomSheet(
    selectedWeek: Int,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val weeks = (1..20).toList()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedWeek - 1).coerceAtLeast(0))
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(bottom = 12.dp)
        ) {
            items(weeks) { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWeekSelected(week) }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "第 ${week} 周",
                        color = if (week == selectedWeek) Color(0xFF1D2939) else Color(0xFF667085)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePeriodColumn(
    month: Int,
    slotHeights: List<Dp>,
    blockSpacing: Dp,
    dayHeaderHeight: Dp,
    headerBottomGap: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        MonthLabel(month = month, height = dayHeaderHeight)
        Spacer(modifier = Modifier.height(headerBottomGap))
        TimePeriodLabel(text = "上午", height = slotHeights[0] + blockSpacing + slotHeights[1])
        Spacer(modifier = Modifier.height(blockSpacing))
        TimePeriodLabel(text = "下午", height = slotHeights[2] + blockSpacing + slotHeights[3])
        Spacer(modifier = Modifier.height(blockSpacing))
        TimePeriodLabel(text = "晚上", height = slotHeights[4])
    }
}

@Composable
private fun MonthLabel(month: Int, height: Dp) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${month}\n月",
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF344054)
        )
    }
}

@Composable
private fun TimePeriodLabel(text: String, height: Dp) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.toVerticalText(),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF344054)
        )
    }
}

@Composable
private fun DayColumn(
    dayOfMonth: Int,
    weekDay: String,
    isToday: Boolean,
    weekDayIndex: Int,
    dayWidth: Dp,
    slotHeights: List<Dp>,
    blockSpacing: Dp,
    dayHeaderHeight: Dp,
    headerBottomGap: Dp,
    weekCourses: List<CourseSlotVo>,
    onCourseClick: ((CourseSlotVo) -> Unit)?,
    onEmptySlotClick: ((weekDay: Int, periodIndex: Int, startSection: Int, defaultSectionCount: Int) -> Unit)?
) {
    val periodRanges = listOf(1..2, 3..5, 6..8, 9..10, 11..13)
    val periodStarts = listOf(1, 3, 6, 9, 11)
    val periodDefaultSectionCounts = listOf(2, 3, 3, 2, 3)
    val dayCourses = weekCourses.filter { it.weekDay == weekDayIndex }

    Column(modifier = Modifier.width(dayWidth).fillMaxHeight()) {
        DayHeader(
            dayOfMonth = dayOfMonth,
            weekDay = weekDay,
            isToday = isToday,
            modifier = Modifier.height(dayHeaderHeight).fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(headerBottomGap))
        slotHeights.forEachIndexed { index, height ->
            val matchedCourse = dayCourses.firstOrNull { slot -> slot.startSection in periodRanges[index] }
            val blockColor = if (matchedCourse == null) {
                Color(CourseColorPalette.emptySlotColor)
            } else {
                val palette = CourseColorPalette.presetColors
                Color(palette[matchedCourse.colorIndex.mod(palette.size)])
            }
            CourseBlock(
                color = blockColor,
                height = height,
                width = dayWidth,
                courseName = matchedCourse?.courseName.orEmpty(),
                classroom = matchedCourse?.location.orEmpty(),
                onClick = {
                    if (matchedCourse == null) {
                        onEmptySlotClick?.invoke(
                            weekDayIndex,
                            index,
                            periodStarts[index],
                            periodDefaultSectionCounts[index]
                        )
                    } else {
                        onCourseClick?.invoke(matchedCourse)
                    }
                }
            )
            if (index != slotHeights.lastIndex) {
                Spacer(modifier = Modifier.height(blockSpacing))
            }
        }
    }
}

@Composable
private fun DayHeader(
    dayOfMonth: Int,
    weekDay: String,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isToday) Color(0x332565FF) else Color.Transparent
    val textColor = if (isToday) Color(0xFF1D4ED8) else Color(0xFF344054)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = bgColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(text = dayOfMonth.toString(), fontSize = 12.sp, lineHeight = 12.sp, color = textColor)
                Text(text = weekDay, fontSize = 12.sp, lineHeight = 12.sp, color = textColor)
            }
        }
    }
}

@Composable
private fun CourseBlock(
    color: Color,
    height: Dp,
    width: Dp,
    courseName: String = "",
    classroom: String = "",
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = color,
        modifier = Modifier
            .size(width = width, height = height)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), // 或 fillMaxHeight()
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = courseName,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding( top =4.dp, start = 4.dp,end = 4.dp),
                maxLines = 3,
                color = Color.White,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = classroom,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp,end = 4.dp),
                maxLines = 3,
                color = Color.White,
                overflow = TextOverflow.Ellipsis
                )
        }
    }
}

private fun calculateVisibleDays(
    contentWidth: Dp,
    minDayWidth: Dp,
    spacing: Dp
): Int {
    val value = floor(((contentWidth + spacing) / (minDayWidth + spacing)).toDouble()).toInt()
    return max(1, value)
}

private fun calculateDayWidth(contentWidth: Dp, visibleDays: Int, spacing: Dp): Dp {
    val totalSpacing = spacing * (visibleDays - 1)
    return ((contentWidth - totalSpacing) / visibleDays).coerceAtLeast(0.dp)
}

private fun calculateSlotHeights(totalHeight: Dp, spacing: Dp, weights: List<Float>): List<Dp> {
    val blockCount = weights.size
    val usableHeight = (totalHeight - spacing * (blockCount - 1)).coerceAtLeast(0.dp)
    val weightSum = weights.sum().takeIf { it > 0f } ?: 1f
    return weights.map { weight -> usableHeight * (weight / weightSum) }
}

private fun String.toVerticalText(): String = toCharArray().joinToString(separator = "\n")

private fun buildWeekSchedules(semesterStartDate: LocalDate, selectedWeek: Int): List<DaySchedule> {
    val normalizedWeek = selectedWeek.coerceAtLeast(1)
    // Anchor week 1 to the Monday of the semester start date's week.
    val daysFromMonday = (semesterStartDate.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
    val firstWeekMonday = semesterStartDate.minusDays(daysFromMonday)
    val weekStartDate = firstWeekMonday.plusDays((normalizedWeek - 1) * 7L)
    return (0..6).map { offset ->
        val date = weekStartDate.plusDays(offset.toLong())
        DaySchedule(
            month = date.monthValue,
            dayOfMonth = date.dayOfMonth,
            weekDay = date.dayOfWeek.toChineseWeekDay()
        )
    }
}

private fun DayOfWeek.toChineseWeekDay(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}

data class DaySchedule(
    val month: Int,
    val dayOfMonth: Int,
    val weekDay: String
)

