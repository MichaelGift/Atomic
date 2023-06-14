package com.myth.atomic.domain.usecases

import com.myth.atomic.data.model.Goals
import com.myth.atomic.domain.repository.Repository

class UpdateGoalUseCase(private val repository: Repository) {
    suspend fun execute(goal: Goals)= repository.updateGoal(goal)
}