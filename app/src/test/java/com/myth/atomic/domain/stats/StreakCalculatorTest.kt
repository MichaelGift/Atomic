package com.myth.atomic.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class StreakCalculatorTest {
    private val today = LocalDate.of(2026, 3, 14)

    private fun days(vararg daysAgo: Long) = daysAgo.map { today.minusDays(it) }

    @Test
    fun noHistoryMeansNoStreak() {
        val stats = StreakCalculator.calculate(emptyList(), today)

        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.longestStreak)
        assertEquals(7, stats.lastSevenDays.size)
        assertEquals(0, stats.completedThisWeek)
    }

    @Test
    fun streakIncludesToday() {
        assertEquals(3, StreakCalculator.calculate(days(0, 1, 2, 4), today).currentStreak)
    }

    @Test
    fun streakSurvivesUntilTodayEnds() {
        // Nothing done yet today, but yesterday's run is still alive.
        assertEquals(2, StreakCalculator.calculate(days(1, 2), today).currentStreak)
    }

    @Test
    fun missedDayBreaksStreak() {
        assertEquals(0, StreakCalculator.calculate(days(2, 3, 4), today).currentStreak)
    }

    @Test
    fun longestStreakAndDuplicates() {
        // Several tasks on one day count once towards the streak.
        val stats = StreakCalculator.calculate(days(0, 0, 0, 10, 11, 12, 13, 20), today)

        assertEquals(1, stats.currentStreak)
        assertEquals(4, stats.longestStreak)
    }

    @Test
    fun weekCountsAreOldestFirstAndEndToday() {
        val stats = StreakCalculator.calculate(days(0, 0, 6, 7), today)

        assertEquals(today.minusDays(6), stats.lastSevenDays.first().date)
        assertEquals(today, stats.lastSevenDays.last().date)
        assertEquals(listOf(1, 0, 0, 0, 0, 0, 2), stats.lastSevenDays.map { it.count })
        assertEquals(3, stats.completedThisWeek)
    }

    @Test
    fun heatmapHistoryStartsOnAMondayAndCoversTwelveWeeks() {
        // 2026-03-14 is a Saturday, so the current week has six days so far.
        val stats = StreakCalculator.calculate(days(0, 0, 80), today)

        assertEquals(DayOfWeek.MONDAY, stats.days.first().date.dayOfWeek)
        assertEquals(today, stats.days.last().date)
        assertEquals(11 * 7 + 6, stats.days.size)
        assertEquals(2, stats.completedToday)
        assertEquals(1, stats.days.first { it.date == today.minusDays(80) }.count)
    }

    @Test
    fun ignoresFutureDates() {
        assertEquals(0, StreakCalculator.calculate(days(-1), today).longestStreak)
    }
}
