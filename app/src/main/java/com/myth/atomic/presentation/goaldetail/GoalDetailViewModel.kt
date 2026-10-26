package com.myth.atomic.presentation.goaldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.model.RepeatInterval
import com.myth.atomic.domain.model.Task
import com.myth.atomic.domain.reminders.AddReminderUseCase
import com.myth.atomic.domain.reminders.DeleteReminderUseCase
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.TaskRepository
import com.myth.atomic.presentation.navigation.GoalDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

sealed interface GoalDetailUiState {
    data object Loading : GoalDetailUiState

    /** The goal does not exist, or was just deleted. */
    data object NotFound : GoalDetailUiState

    data class Loaded(val details: GoalDetails, val today: LocalDate) : GoalDetailUiState {
        val completedTasks: Int get() = details.tasks.count { it.isCompleted }
        val progress: Float get() = if (details.tasks.isEmpty()) 0f else completedTasks.toFloat() / details.tasks.size
    }
}

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    private val addReminderUseCase: AddReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val clock: Clock,
) : ViewModel() {
    private val goalId = savedStateHandle.toRoute<GoalDetailRoute>().goalId

    val uiState: StateFlow<GoalDetailUiState> = goalRepository.observeGoalDetails(goalId)
        .map { details ->
            if (details == null) GoalDetailUiState.NotFound else GoalDetailUiState.Loaded(details, LocalDate.now(clock))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalDetailUiState.Loading)

    fun updateGoal(title: String, description: String, deadline: LocalDate?, colorIndex: Int) {
        val goal = (uiState.value as? GoalDetailUiState.Loaded)?.details?.goal ?: return
        viewModelScope.launch {
            goalRepository.updateGoal(
                goal.copy(
                    title = title.trim(),
                    description = description.trim(),
                    deadline = deadline?.toInstant(),
                    colorIndex = colorIndex,
                ),
            )
        }
    }

    fun setGoalCompleted(completed: Boolean) {
        viewModelScope.launch { goalRepository.setGoalCompleted(goalId, completed) }
    }

    fun deleteGoal() {
        val reminders = (uiState.value as? GoalDetailUiState.Loaded)?.details?.reminders.orEmpty()
        viewModelScope.launch {
            // The database cascade removes the rows, but the alarms must be cancelled explicitly.
            reminders.forEach { deleteReminderUseCase(it.id) }
            goalRepository.deleteGoal(goalId)
        }
    }

    fun addReminder(title: String, date: LocalDate, time: LocalTime, repeat: RepeatInterval) {
        viewModelScope.launch {
            addReminderUseCase(
                Reminder(
                    title = title.trim(),
                    triggerAt = date.atTime(time).atZone(clock.zone).toInstant(),
                    repeat = repeat,
                    goalId = goalId,
                ),
            )
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch { deleteReminderUseCase(reminderId) }
    }

    fun addTask(title: String, dueDate: LocalDate?) {
        viewModelScope.launch {
            taskRepository.addTask(
                Task(goalId = goalId, title = title.trim(), dueAt = dueDate?.toInstant(), createdAt = clock.instant()),
            )
        }
    }

    fun setTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { taskRepository.setTaskCompleted(taskId, completed) }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch { taskRepository.deleteTask(taskId) }
    }

    private fun LocalDate.toInstant() = atStartOfDay(clock.zone).toInstant()
}
