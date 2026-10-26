package com.myth.atomic.data.repository

import com.myth.atomic.data.local.dao.GoalDao
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.GoalWithProgress
import com.myth.atomic.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class RoomGoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val clock: Clock,
) : GoalRepository {
    override fun observeGoalsWithProgress(): Flow<List<GoalWithProgress>> =
        goalDao.observeWithTaskCounts().map { rows -> rows.map { it.toDomain() } }

    override fun observeGoalDetails(goalId: Long): Flow<GoalDetails?> =
        goalDao.observeWithChildren(goalId).map { it?.toDomain() }

    override fun searchGoals(query: String): Flow<List<Goal>> =
        goalDao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getGoal(goalId: Long): Goal? = goalDao.getById(goalId)?.toDomain()

    override suspend fun addGoal(goal: Goal): Long = goalDao.insert(goal.toEntity())

    override suspend fun updateGoal(goal: Goal) = goalDao.update(goal.toEntity())

    override suspend fun setGoalCompleted(goalId: Long, completed: Boolean) =
        goalDao.setCompletedAt(goalId, if (completed) clock.instant() else null)

    override suspend fun deleteGoal(goalId: Long) = goalDao.deleteById(goalId)
}
