package com.myth.atomic.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.atomic.domain.model.Task
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.TaskRepository
import com.myth.atomic.domain.stats.ObserveProgressStatsUseCase
import com.myth.atomic.domain.stats.ProgressStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class TaskWithGoal(val task: Task, val goalTitle: String)

data class TodayUiState(
    val date: LocalDate,
    val isLoading: Boolean = true,
    val overdue: List<TaskWithGoal> = emptyList(),
    val dueToday: List<TaskWithGoal> = emptyList(),
    val stats: ProgressStats? = null,
) {
    val isEmpty: Boolean get() = overdue.isEmpty() && dueToday.isEmpty()
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    observeProgressStats: ObserveProgressStatsUseCase,
    private val clock: Clock,
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now(clock))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TodayUiState> = today.flatMapLatest { date ->
        val startOfToday = date.atStartOfDay(clock.zone).toInstant()
        val startOfTomorrow = date.plusDays(1).atStartOfDay(clock.zone).toInstant()
        combine(
            taskRepository.observeOpenTasksDueBefore(startOfTomorrow),
            goalRepository.observeGoalsWithProgress(),
            observeProgressStats(date),
        ) { tasks, goals, stats ->
            val goalTitles = goals.associate { it.goal.id to it.goal.title }
            val items = tasks.map { TaskWithGoal(it, goalTitles[it.goalId].orEmpty()) }
            val (overdue, dueToday) = items.partition { it.task.dueAt!! < startOfToday }
            TodayUiState(date = date, isLoading = false, overdue = overdue, dueToday = dueToday, stats = stats)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState(date = today.value))

    /** Called when the screen resumes, so the lists move on if the app was left open past midnight. */
    fun refreshDate() {
        today.value = LocalDate.now(clock)
    }

    fun setTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { taskRepository.setTaskCompleted(taskId, completed) }
    }
}
