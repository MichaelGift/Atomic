package com.myth.atomic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Reminders")
data class Reminder
    (
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val reminderTitle: String,
    val reminderDate: Long,
    val reminderTime: Long,
    val isRepeatReminder: Boolean
)