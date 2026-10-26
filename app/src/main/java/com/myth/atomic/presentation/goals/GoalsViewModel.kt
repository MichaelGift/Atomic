package com.myth.atomic.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalWithProgress
import com.myth.atomic.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class GoalsUiState(
    val today: LocalDate,
    val isLoading: Boolean = true,
    val active: List<GoalWithProgress> = emptyList(),
    val completed: List<GoalWithProgress> = emptyList(),
) {
    val isEmpty: Boolean get() = active.isEmpty() && completed.isEmpty()

    /** Cycle through the palette so new goals look distinct by default. */
    val nextColorIndex: Int get() = active.size + completed.size
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<GoalsUiState> = goalRepository.observeGoalsWithProgress()
        .map { goals ->
            val (completed, active) = goals.partition { it.goal.isCompleted }
            GoalsUiState(today = LocalDate.now(clock), isLoading = false, active = active, completed = completed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState(today = LocalDate.now(clock)))

    fun addGoal(title: String, description: String, deadline: LocalDate?, colorIndex: Int) {
        viewModelScope.launch {
            goalRepository.addGoal(
                Goal(
                    title = title.trim(),
                    description = description.trim(),
                    deadline = deadline?.atStartOfDay(clock.zone)?.toInstant(),
                    createdAt = clock.instant(),
                    colorIndex = colorIndex,
                ),
            )
        }
    }
}
