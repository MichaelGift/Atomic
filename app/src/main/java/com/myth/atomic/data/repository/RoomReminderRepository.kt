package com.myth.atomic.data.repository

import com.myth.atomic.data.local.dao.ReminderDao
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
) : ReminderRepository {
    override fun observeReminders(): Flow<List<Reminder>> =
        reminderDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun searchReminders(query: String): Flow<List<Reminder>> =
        reminderDao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getReminder(reminderId: Long): Reminder? = reminderDao.getById(reminderId)?.toDomain()

    override suspend fun getAllReminders(): List<Reminder> = reminderDao.getAll().map { it.toDomain() }

    override suspend fun addReminder(reminder: Reminder): Long = reminderDao.insert(reminder.toEntity())

    override suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder.toEntity())

    override suspend fun deleteReminder(reminderId: Long) = reminderDao.deleteById(reminderId)
}
