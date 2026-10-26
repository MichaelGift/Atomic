package com.myth.atomic.domain.stats

import com.myth.atomic.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class DayCount(val date: LocalDate, val count: Int)

data class ProgressStats(
    /** Consecutive days with at least one completed task, ending today (or yesterday, if today is still open). */
    val currentStreak: Int,
    val longestStreak: Int,
    /** The last seven days, oldest first, ending today. */
    val lastSevenDays: List<DayCount>,
) {
    val completedThisWeek: Int get() = lastSevenDays.sumOf { it.count }
    val hasHistory: Boolean get() = longestStreak > 0
}

object StreakCalculator {
    fun calculate(completionDates: List<LocalDate>, today: LocalDate): ProgressStats {
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

        val lastSevenDays = (6L downTo 0L).map { today.minusDays(it) }.map { DayCount(it, counts[it] ?: 0) }
        return ProgressStats(current, longest, lastSevenDays)
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
