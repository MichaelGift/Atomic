package com.myth.atomic.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalWithProgress
import com.myth.atomic.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {
    val goals: StateFlow<List<GoalWithProgress>> = goalRepository.observeGoalsWithProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGoal(title: String, description: String = "") {
        viewModelScope.launch {
            goalRepository.addGoal(Goal(title = title.trim(), description = description.trim()))
        }
    }

    fun setCompleted(goalId: Long, completed: Boolean) {
        viewModelScope.launch { goalRepository.setGoalCompleted(goalId, completed) }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch { goalRepository.deleteGoal(goalId) }
    }
}
