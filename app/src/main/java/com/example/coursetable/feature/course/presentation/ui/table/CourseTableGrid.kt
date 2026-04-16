package com.example.coursetable.feature.course.presentation.ui.table

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.domain.model.CourseColorPalette
import com.example.coursetable.domain.model.CourseSlotVo

@Composable
fun TimePeriodColumn(
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
fun DayColumn(
    dayOfMonth: Int,
    weekDay: String,
    isToday: Boolean,
    weekDayIndex: Int,
    dayWidth: Dp,
    slotHeights: List<Dp>,
    blockSpacing: Dp,
    dayHeaderHeight: Dp,
    headerBottomGap: Dp,
    selectedWeek: Int,
    weekCourses: List<CourseSlotVo>,
    onCourseClick: ((CourseSlotVo) -> Unit)?,
    onEmptySlotClick: ((weekDay: Int, periodIndex: Int) -> Unit)?
) {
    val dayCourses = weekCourses.filter { it.weekDay == weekDayIndex }

    Column(modifier = Modifier.width(dayWidth).fillMaxHeight()) {
        DayHeader(
            dayOfMonth = dayOfMonth,
            weekDay = weekDay,
            isToday = isToday,
            modifier = Modifier
                .height(dayHeaderHeight)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(headerBottomGap))
        slotHeights.forEachIndexed { index, height ->
            val periodSlot = getPeriodSlot(index)
            val matchedCourse = dayCourses
                .filter { slot ->
                    val slotEnd = slot.startSection + slot.sectionCount - 1
                    slot.startSection <= periodSlot.endSection && slotEnd >= periodSlot.startSection
                }
                .minWithOrNull(
                    compareBy<CourseSlotVo>(
                        { if (selectedWeek in it.weekStart..it.weekEnd) 0 else 1 },
                        { it.sectionCount },
                        { kotlin.math.abs(it.startSection - periodSlot.startSection) }
                    )
                )
            CoursePeriodBlock(
                height = height,
                width = dayWidth,
                periodSlot = periodSlot,
                slot = matchedCourse,
                selectedWeek = selectedWeek,
                onClick = {
                    if (matchedCourse == null) {
                        onEmptySlotClick?.invoke(weekDayIndex, index)
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
private fun MonthLabel(month: Int, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
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
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
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
private fun CoursePeriodBlock(
    height: Dp,
    width: Dp,
    periodSlot: PeriodSlot,
    slot: CourseSlotVo?,
    selectedWeek: Int,
    onClick: (() -> Unit)? = null
) {
    val sectionSize = (periodSlot.endSection - periodSlot.startSection + 1).coerceAtLeast(1)
    val sectionHeight = height / sectionSize

    val slotStart = slot?.startSection ?: periodSlot.startSection
    val slotEnd = slot?.let { it.startSection + it.sectionCount - 1 } ?: periodSlot.endSection
    val visibleStart = slotStart.coerceIn(periodSlot.startSection, periodSlot.endSection)
    val visibleEnd = slotEnd.coerceIn(periodSlot.startSection, periodSlot.endSection)
    val occupiedSections = (visibleEnd - visibleStart + 1).coerceAtLeast(1)
    val topOffsetSections = (visibleStart - periodSlot.startSection).coerceAtLeast(0)
    val bottomOffsetSections = (sectionSize - topOffsetSections - occupiedSections).coerceAtLeast(0)

    val topOffset = sectionHeight * topOffsetSections
    val occupiedHeight = sectionHeight * occupiedSections
    val bottomOffset = sectionHeight * bottomOffsetSections
    val isCurrentWeek = slot?.let { selectedWeek in it.weekStart..it.weekEnd } ?: false

    val courseColor = if (slot == null) {
        Color(CourseColorPalette.emptySlotColor)
    } else if (!isCurrentWeek) {
        Color(CourseColorPalette.emptySlotColor)
    } else {
        val palette = CourseColorPalette.presetColors
        Color(palette[slot.colorIndex.mod(palette.size)])
    }
    val slotContainerColor = if (slot == null) {
        Color(CourseColorPalette.emptySlotColor)
    } else {
        MaterialTheme.colorScheme.background
    }

    Surface(
        color = slotContainerColor,
        modifier = Modifier
            .size(width = width, height = height)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(8.dp)
    ) {
        if (slot != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (topOffset > 0.dp) Spacer(modifier = Modifier.height(topOffset))
                Surface(
                    color = courseColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(occupiedHeight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCurrentWeek) slot.courseName else "【非本周】${slot.courseName}",
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                            maxLines = 3,
                            color = Color.White,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = slot.location,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp),
                            maxLines = 3,
                            color = Color.White,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (bottomOffset > 0.dp) Spacer(modifier = Modifier.height(bottomOffset))
            }
        }
    }
}

private fun String.toVerticalText(): String = toCharArray().joinToString(separator = "\n")
