package com.myth.atomic.database

interface LocalDataSource {
    suspend fun AddGoal()
    suspend fun AppJournal()
    suspend fun AddReminder()
    suspend fun AddTask()
    suspend fun editGoal()
    suspend fun editJournal()
    suspend fun editReminder()
    suspend fun editTask()
    suspend fun deleteGoal()
    suspend fun deleteJournal()
    suspend fun deleteReminder()
    suspend fun deleteTask()
    fun getAllGoals()
    fun getAllJournals()
    fun getAllReminders()
    fun getAllTasks()
}