package com.myth.atomic.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.atomic.domain.model.Task
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
) {
    val isEmpty: Boolean get() = overdue.isEmpty() && dueToday.isEmpty()
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val startOfToday = today.atStartOfDay(clock.zone).toInstant()
    private val startOfTomorrow = today.plusDays(1).atStartOfDay(clock.zone).toInstant()

    val uiState: StateFlow<TodayUiState> = combine(
        taskRepository.observeOpenTasksDueBefore(startOfTomorrow),
        goalRepository.observeGoalsWithProgress(),
    ) { tasks, goals ->
        val goalTitles = goals.associate { it.goal.id to it.goal.title }
        val items = tasks.map { TaskWithGoal(it, goalTitles[it.goalId].orEmpty()) }
        val (overdue, dueToday) = items.partition { it.task.dueAt!! < startOfToday }
        TodayUiState(date = today, isLoading = false, overdue = overdue, dueToday = dueToday)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState(date = today))

    fun setTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { taskRepository.setTaskCompleted(taskId, completed) }
    }
}
