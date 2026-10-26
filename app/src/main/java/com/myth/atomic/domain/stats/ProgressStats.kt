package com.myth.atomic.domain.stats

import com.myth.atomic.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class DayCount(val date: LocalDate, val count: Int)

data class ProgressStats(
    /** Consecutive days with at least one completed task, ending today (or yesterday, if today is still open). */
    val currentStreak: Int,
    val longestStreak: Int,
    /** Every day from the start of the heatmap's first week up to today, oldest first. */
    val days: List<DayCount>,
) {
    val lastSevenDays: List<DayCount> get() = days.takeLast(7)
    val completedThisWeek: Int get() = lastSevenDays.sumOf { it.count }
    val completedToday: Int get() = days.lastOrNull()?.count ?: 0
    val hasHistory: Boolean get() = longestStreak > 0
}

object StreakCalculator {
    /** Weeks of history kept for the activity heatmap, including the current one. */
    const val HISTORY_WEEKS = 12L

    fun calculate(
        completionDates: List<LocalDate>,
        today: LocalDate,
        weekStart: DayOfWeek = DayOfWeek.MONDAY,
    ): ProgressStats {
        val counts = completionDates.filter { !it.isAfter(today) }.groupingBy { it }.eachCount()

        var longest = 0
        var run = 0
        var previous: LocalDate? = null
        for (day in counts.keys.sorted()) {
            run = if (previous?.plusDays(1) == day) run + 1 else 1
            longest = maxOf(longest, run)
            previous = day
        }

        // A streak is still alive until a whole day passes with nothing done.
        var cursor = if (today in counts) today else today.minusDays(1)
        var current = 0
        while (cursor in counts) {
            current++
            cursor = cursor.minusDays(1)
        }

        // Start on a week boundary so the heatmap's columns are whole weeks.
        val start = today.with(TemporalAdjusters.previousOrSame(weekStart)).minusWeeks(HISTORY_WEEKS - 1)
        val days = generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .map { DayCount(it, counts[it] ?: 0) }
            .toList()
        return ProgressStats(current, longest, days)
    }
}

class ObserveProgressStatsUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val clock: Clock,
) {
    operator fun invoke(today: LocalDate): Flow<ProgressStats> = taskRepository.observeCompletionTimes().map { times ->
        StreakCalculator.calculate(times.map { it.atZone(clock.zone).toLocalDate() }, today)
    }
}
