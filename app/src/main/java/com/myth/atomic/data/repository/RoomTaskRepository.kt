package com.myth.atomic.data.repository

import com.myth.atomic.data.local.dao.TaskDao
import com.myth.atomic.domain.model.Task
import com.myth.atomic.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class RoomTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val clock: Clock,
) : TaskRepository {
    override fun observeTasksForGoal(goalId: Long): Flow<List<Task>> =
        taskDao.observeForGoal(goalId).map { rows -> rows.map { it.toDomain() } }

    override fun observeOpenTasksDueBefore(end: Instant): Flow<List<Task>> =
        taskDao.observeOpenDueBefore(end).map { rows -> rows.map { it.toDomain() } }

    override fun searchTasks(query: String): Flow<List<Task>> =
        taskDao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun addTask(task: Task): Long = taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

    override suspend fun setTaskCompleted(taskId: Long, completed: Boolean) =
        taskDao.setCompletedAt(taskId, if (completed) clock.instant() else null)

    override suspend fun deleteTask(taskId: Long) = taskDao.deleteById(taskId)
}
