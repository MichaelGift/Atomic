package com.myth.atomic.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.myth.atomic.model.Reminder

@Dao
interface ReminderDao {
    @Insert
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("SELECT * FROM Reminders WHERE reminderTitle LIKE :query")
    fun getAllReminders(query:String):LiveData<List<Reminder>>
}