package com.myth.atomic.domain.repository

import com.myth.atomic.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface TaskRepository {
    fun observeTasksForGoal(goalId: Long): Flow<List<Task>>

    /** Open tasks due before [end], overdue ones included, soonest first. */
    fun observeOpenTasksDueBefore(end: Instant): Flow<List<Task>>
    fun searchTasks(query: String): Flow<List<Task>>

    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
    suspend fun deleteTask(taskId: Long)
}
