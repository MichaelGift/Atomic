package com.myth.atomic.testing

import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.GoalWithProgress
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.model.Task
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.JournalRepository
import com.myth.atomic.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.Instant

/** In-memory stand-in for the Room repositories, for ViewModel tests. */
class FakeAtomicRepository(private val clock: Clock) : GoalRepository, TaskRepository, JournalRepository {
    val goals = MutableStateFlow<List<Goal>>(emptyList())
    val tasks = MutableStateFlow<List<Task>>(emptyList())
    val entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    private var nextId = 1L

    // Goals

    override fun observeGoalsWithProgress(): Flow<List<GoalWithProgress>> = combine(goals, tasks) { goals, tasks ->
        goals.map { goal ->
            val own = tasks.filter { it.goalId == goal.id }
            GoalWithProgress(goal, own.size, own.count { it.isCompleted })
        }
    }

    override fun observeGoalDetails(goalId: Long): Flow<GoalDetails?> = combine(goals, tasks, entries) { goals, tasks, entries ->
        goals.firstOrNull { it.id == goalId }?.let { goal ->
            GoalDetails(goal, tasks.filter { it.goalId == goalId }, emptyList(), entries.filter { it.goalId == goalId })
        }
    }

    override fun searchGoals(query: String): Flow<List<Goal>> =
        goals.map { all -> all.filter { it.title.contains(query, ignoreCase = true) } }

    override suspend fun addGoal(goal: Goal): Long = nextId++.also { id -> goals.update { it + goal.copy(id = id) } }

    override suspend fun updateGoal(goal: Goal) = goals.update { all -> all.map { if (it.id == goal.id) goal else it } }

    override suspend fun setGoalCompleted(goalId: Long, completed: Boolean) =
        goals.update { all -> all.map { if (it.id == goalId) it.copy(completedAt = completedAt(completed)) else it } }

    override suspend fun deleteGoal(goalId: Long) {
        tasks.update { all -> all.filterNot { it.goalId == goalId } }
        entries.update { all -> all.map { if (it.goalId == goalId) it.copy(goalId = null) else it } }
        goals.update { all -> all.filterNot { it.id == goalId } }
    }

    // Tasks

    override fun observeTasksForGoal(goalId: Long): Flow<List<Task>> = tasks.map { all -> all.filter { it.goalId == goalId } }

    override fun observeOpenTasksDueBefore(end: Instant): Flow<List<Task>> = tasks.map { all ->
        all.filter { !it.isCompleted && it.dueAt != null && it.dueAt < end }.sortedBy { it.dueAt }
    }

    override fun searchTasks(query: String): Flow<List<Task>> =
        tasks.map { all -> all.filter { it.title.contains(query, ignoreCase = true) } }

    override suspend fun addTask(task: Task): Long = nextId++.also { id -> tasks.update { it + task.copy(id = id) } }

    override suspend fun updateTask(task: Task) = tasks.update { all -> all.map { if (it.id == task.id) task else it } }

    override suspend fun setTaskCompleted(taskId: Long, completed: Boolean) =
        tasks.update { all -> all.map { if (it.id == taskId) it.copy(completedAt = completedAt(completed)) else it } }

    override suspend fun deleteTask(taskId: Long) = tasks.update { all -> all.filterNot { it.id == taskId } }

    // Journal

    override fun observeEntries(): Flow<List<JournalEntry>> = entries.map { all -> all.sortedByDescending { it.createdAt } }

    override fun observeEntriesForGoal(goalId: Long): Flow<List<JournalEntry>> =
        entries.map { all -> all.filter { it.goalId == goalId } }

    override fun searchEntries(query: String): Flow<List<JournalEntry>> = entries.map { all ->
        all.filter { it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true) }
    }

    override suspend fun getEntry(entryId: Long): JournalEntry? = entries.value.firstOrNull { it.id == entryId }

    override suspend fun addEntry(entry: JournalEntry): Long = nextId++.also { id -> entries.update { it + entry.copy(id = id) } }

    override suspend fun updateEntry(entry: JournalEntry) =
        entries.update { all -> all.map { if (it.id == entry.id) entry else it } }

    override suspend fun deleteEntry(entryId: Long) = entries.update { all -> all.filterNot { it.id == entryId } }

    private fun completedAt(completed: Boolean) = if (completed) clock.instant() else null
}
