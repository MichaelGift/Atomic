package com.myth.atomic.data.datasource

import androidx.lifecycle.LiveData
import com.myth.atomic.data.model.Goals
import com.myth.atomic.data.model.Journal
import com.myth.atomic.data.model.Reminder
import com.myth.atomic.data.model.Task

interface LocalDataSource {
    suspend fun AddGoal(goals: Goals)
    suspend fun AddJournal(journal: Journal)
    suspend fun AddReminder(reminder: Reminder)
    suspend fun AddTask(task: Task)
    suspend fun editGoal(goals: Goals)
    suspend fun editJournal(journal: Journal)
    suspend fun editReminder(reminder: Reminder)
    suspend fun editTask(task: Task)
    suspend fun deleteGoal(goals: Goals)
    suspend fun deleteJournal(journal: Journal)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun deleteTask(task: Task)
    fun searchGoals(query: String):LiveData<List<Goals>>
    fun searchJournals(query: String):LiveData<List<Journal>>
    fun searchReminders(query: String):LiveData<List<Reminder>>
    fun searchTasks(query: Int):LiveData<List<Task>>
    fun getAllGoals():LiveData<List<Goals>>
    fun getAllJournals():LiveData<List<Journal>>
    fun getAllReminders():LiveData<List<Reminder>>
    fun getAllTasks():LiveData<List<Task>>
}