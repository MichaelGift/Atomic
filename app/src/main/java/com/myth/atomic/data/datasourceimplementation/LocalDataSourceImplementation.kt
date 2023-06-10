package com.myth.atomic.data.datasourceimplementation

import androidx.lifecycle.LiveData
import com.myth.atomic.data.db.GoalDao
import com.myth.atomic.data.db.JournalDao
import com.myth.atomic.data.db.ReminderDao
import com.myth.atomic.data.db.TaskDao
import com.myth.atomic.data.datasource.LocalDataSource
import com.myth.atomic.data.model.Goals
import com.myth.atomic.data.model.Journal
import com.myth.atomic.data.model.Reminder
import com.myth.atomic.data.model.Task

class LocalDataSourceImplementation(
    val goalDao: GoalDao,
    val journalDao: JournalDao,
    val reminderDao: ReminderDao,
    val taskDao: TaskDao
): LocalDataSource {
    override suspend fun AddGoal(goals: Goals) = goalDao.insertGoal(goals)
    override suspend fun AddJournal(journal: Journal) =journalDao.insertJournal(journal)
    override suspend fun AddReminder(reminder: Reminder) =reminderDao.insertReminder(reminder)
    override suspend fun AddTask(task: Task) =taskDao.insertTask(task)

    override suspend fun editGoal(goals: Goals) = goalDao.updateGoal(goals)
    override suspend fun editJournal(journal: Journal) = journalDao.updateJournal(journal)
    override suspend fun editReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)
    override suspend fun editTask(task: Task) = taskDao.updateTask(task)

    override suspend fun deleteGoal(goals: Goals) =goalDao.deleteGoal(goals)
    override suspend fun deleteJournal(journal: Journal) =journalDao.deleteJournal(journal)
    override suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)
    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)


    override fun searchGoals(query: String): LiveData<List<Goals>> {
        return goalDao.searchGoals(query)
    }

    override fun searchJournals(query: String): LiveData<List<Journal>> {
        return journalDao.searchJournals(query)
    }

    override fun searchReminders(query: String): LiveData<List<Reminder>> {
        return reminderDao.searchReminders(query)
    }

    override fun searchTasks(query: Int): LiveData<List<Task>> {
        return taskDao.searchTasks(query)
    }

    override fun getAllGoals(): LiveData<List<Goals>> {
        return goalDao.getAllGoals()
    }

    override fun getAllJournals(): LiveData<List<Journal>> {
        return journalDao.getAllJournals()
    }

    override fun getAllReminders(): LiveData<List<Reminder>> {
        return reminderDao.getAllReminders()
    }

    override fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }
}