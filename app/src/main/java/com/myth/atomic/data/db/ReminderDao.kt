package com.myth.atomic.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.myth.atomic.data.model.Reminder

@Dao
interface ReminderDao {
    @Insert
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("SELECT * FROM Reminders WHERE reminderTitle LIKE :query")
    fun searchReminders(query:String):LiveData<List<Reminder>>

    @Query("SELECT * FROM Reminders ORDER BY id DESC")
    fun getAllReminders():LiveData<List<Reminder>>
}