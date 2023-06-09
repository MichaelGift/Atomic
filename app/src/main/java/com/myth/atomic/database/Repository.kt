package com.myth.atomic.database

import androidx.lifecycle.LiveData
import com.myth.atomic.model.Goals
import com.myth.atomic.model.Journal
import com.myth.atomic.model.Reminder
import com.myth.atomic.model.Task

interface Repository {
    suspend fun addGoal(goal: Goals)
    suspend fun addJournal(journal: Journal)
    suspend fun addReminder(reminder: Reminder)
    suspend fun addTask(task: Task)

    suspend fun deleteGoal(goal: Goals)
    suspend fun deleteJournal(journal: Journal)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun deleteTask(task: Task)

    suspend fun updateGoal(goal: Goals)
    suspend fun updateJournal(journal: Journal)
    suspend fun updateReminder(reminder: Reminder)
    suspend fun updateTask(task: Task)

    fun searchGoal(query:String):LiveData<List<Goals>>
    fun searchJournal(query:String):LiveData<List<Journal>>
    fun searchReminder(query:String):LiveData<List<Reminder>>
    fun searchTask(query:Int):LiveData<List<Task>>

    fun getAllGoals():LiveData<List<Goals>>
    fun getAllJournals():LiveData<List<Journal>>
    fun getAllReminders():LiveData<List<Reminder>>
    fun getAllTasks():LiveData<List<Task>>
}