package com.myth.atomic.database

import androidx.lifecycle.LiveData
import com.myth.atomic.model.Goals
import com.myth.atomic.model.Journal
import com.myth.atomic.model.Reminder
import com.myth.atomic.model.Task

class RepositoryImplementation(
    val localDataSource: LocalDataSource
): Repository {
    override suspend fun addGoal(goal: Goals) = localDataSource.AddGoal(goal)
    override suspend fun addJournal(journal: Journal) = localDataSource.AddJournal(journal)
    override suspend fun addReminder(reminder: Reminder) = localDataSource.AddReminder(reminder)
    override suspend fun addTask(task: Task) = localDataSource.AddTask(task)

    override suspend fun deleteGoal(goal: Goals) = localDataSource.deleteGoal(goal)
    override suspend fun deleteJournal(journal: Journal) = localDataSource.deleteJournal(journal)
    override suspend fun deleteReminder(reminder: Reminder) = localDataSource.deleteReminder(reminder)
    override suspend fun deleteTask(task: Task) = localDataSource.deleteTask(task)

    override suspend fun updateGoal(goal: Goals) = localDataSource.editGoal(goal)
    override suspend fun updateJournal(journal: Journal) = localDataSource.editJournal(journal)
    override suspend fun updateReminder(reminder: Reminder) = localDataSource.editReminder(reminder)
    override suspend fun updateTask(task: Task) = localDataSource.editTask(task)

    override fun searchGoal(query: String): LiveData<List<Goals>> {
        return localDataSource.searchGoals(query)
    }

    override fun searchJournal(query: String): LiveData<List<Journal>> {
        return localDataSource.searchJournals(query)
    }

    override fun searchReminder(query: String): LiveData<List<Reminder>> {
        return localDataSource.searchReminders(query)
    }

    override fun searchTask(query: Int): LiveData<List<Task>> {
        return localDataSource.searchTasks(query)
    }

    override fun getAllGoals(): LiveData<List<Goals>> {
        return localDataSource.getAllGoals()
    }

    override fun getAllJournals(): LiveData<List<Journal>> {
        return localDataSource.getAllJournals()
    }

    override fun getAllReminders(): LiveData<List<Reminder>> {
        return localDataSource.getAllReminders()
    }

    override fun getAllTasks(): LiveData<List<Task>> {
        return localDataSource.getAllTasks()
    }
}