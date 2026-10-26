package com.myth.atomic.domain.repository

import com.myth.atomic.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeReminders(): Flow<List<Reminder>>
    fun searchReminders(query: String): Flow<List<Reminder>>

    suspend fun getReminder(reminderId: Long): Reminder?
    suspend fun getAllReminders(): List<Reminder>
    suspend fun addReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminderId: Long)
}
