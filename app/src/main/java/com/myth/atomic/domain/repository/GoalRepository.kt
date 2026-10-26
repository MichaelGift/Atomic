package com.myth.atomic.domain.repository

import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.GoalWithProgress
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoalsWithProgress(): Flow<List<GoalWithProgress>>
    fun observeGoalDetails(goalId: Long): Flow<GoalDetails?>
    fun searchGoals(query: String): Flow<List<Goal>>

    suspend fun getGoal(goalId: Long): Goal?
    suspend fun addGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun setGoalCompleted(goalId: Long, completed: Boolean)

    /** Deletes the goal with its tasks and reminders; its journal entries are kept. */
    suspend fun deleteGoal(goalId: Long)
}
