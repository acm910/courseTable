package com.example.coursetable.feature.course.presentation.ui.table

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.max

internal val slotWeights = listOf(2f, 3f, 3f, 2f, 3f)

data class PeriodSlot(
    val index: Int,
    val startSection: Int,
    val endSection: Int,
    val defaultSectionCount: Int
)

data class SectionRangeOption(
    val startSection: Int,
    val sectionCount: Int
) {
    val endSection: Int
        get() = startSection + sectionCount - 1
}

internal val periodSlots = listOf(
    PeriodSlot(index = 0, startSection = 1, endSection = 2, defaultSectionCount = 2),
    PeriodSlot(index = 1, startSection = 3, endSection = 5, defaultSectionCount = 3),
    PeriodSlot(index = 2, startSection = 6, endSection = 8, defaultSectionCount = 3),
    PeriodSlot(index = 3, startSection = 9, endSection = 10, defaultSectionCount = 2),
    PeriodSlot(index = 4, startSection = 11, endSection = 13, defaultSectionCount = 3)
)

data class DaySchedule(
    val month: Int,
    val dayOfMonth: Int,
    val weekDay: String
)

fun calculateVisibleDays(
    contentWidth: Dp,
    minDayWidth: Dp,
    spacing: Dp
): Int {
    val value = floor(((contentWidth + spacing) / (minDayWidth + spacing)).toDouble()).toInt()
    return max(1, value)
}

fun calculateDayWidth(contentWidth: Dp, visibleDays: Int, spacing: Dp): Dp {
    val totalSpacing = spacing * (visibleDays - 1)
    return ((contentWidth - totalSpacing) / visibleDays).coerceAtLeast(0.dp)
}

fun calculateSlotHeights(totalHeight: Dp, spacing: Dp, weights: List<Float>): List<Dp> {
    val blockCount = weights.size
    val usableHeight = (totalHeight - spacing * (blockCount - 1)).coerceAtLeast(0.dp)
    val weightSum = weights.sum().takeIf { it > 0f } ?: 1f
    return weights.map { weight -> usableHeight * (weight / weightSum) }
}

fun getPeriodSlot(periodIndex: Int): PeriodSlot = periodSlots[periodIndex.coerceIn(0, periodSlots.lastIndex)]

fun findPeriodIndexByStartSection(startSection: Int): Int {
    return periodSlots.indexOfFirst { startSection in it.startSection..it.endSection }
        .takeIf { it >= 0 }
        ?: 0
}

fun buildSectionRangeOptions(periodIndex: Int): List<SectionRangeOption> {
    val slot = getPeriodSlot(periodIndex)
    val sectionSize = slot.endSection - slot.startSection + 1
    return if (sectionSize == 3) {
        listOf(
            SectionRangeOption(startSection = slot.startSection, sectionCount = 3),
            SectionRangeOption(startSection = slot.startSection, sectionCount = 2),
            SectionRangeOption(startSection = slot.startSection + 1, sectionCount = 2)
        )
    } else {
        listOf(SectionRangeOption(startSection = slot.startSection, sectionCount = sectionSize))
    }
}

fun buildWeekSchedules(semesterStartDate: LocalDate, selectedWeek: Int): List<DaySchedule> {
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
