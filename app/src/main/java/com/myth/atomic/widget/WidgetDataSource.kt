package com.myth.atomic.widget

import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.TaskRepository
import com.myth.atomic.domain.stats.ObserveProgressStatsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class WidgetTask(val id: Long, val title: String, val goalTitle: String, val isOverdue: Boolean)

data class WidgetState(val tasks: List<WidgetTask>, val totalDue: Int, val streak: Int, val doneToday: Int = 0) {
    val dayProgress: Float get() = (doneToday + totalDue).let { if (it == 0) 0f else doneToday.toFloat() / it }
}

class WidgetDataSource @Inject constructor(
    private val taskRepository: TaskRepository,
    private val goalRepository: GoalRepository,
    private val observeProgressStats: ObserveProgressStatsUseCase,
    private val clock: Clock,
) {
    fun observe(): Flow<WidgetState> {
        val today = LocalDate.now(clock)
        val startOfToday = today.atStartOfDay(clock.zone).toInstant()
        val startOfTomorrow = today.plusDays(1).atStartOfDay(clock.zone).toInstant()
        return combine(
            taskRepository.observeOpenTasksDueBefore(startOfTomorrow),
            goalRepository.observeGoalsWithProgress(),
            observeProgressStats(today),
        ) { tasks, goals, stats ->
            val goalTitles = goals.associate { it.goal.id to it.goal.title }
            WidgetState(
                tasks = tasks.take(MAX_TASKS).map {
                    WidgetTask(it.id, it.title, goalTitles[it.goalId].orEmpty(), it.dueAt!! < startOfToday)
                },
                totalDue = tasks.size,
                streak = stats.currentStreak,
                doneToday = stats.completedToday,
            )
        }
    }

    suspend fun load(): WidgetState = observe().first()

    private companion object {
        const val MAX_TASKS = 6
    }
}

/** Widgets and their actions are created by the system, outside Hilt's reach. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetDataSource(): WidgetDataSource
    fun taskRepository(): TaskRepository
}
