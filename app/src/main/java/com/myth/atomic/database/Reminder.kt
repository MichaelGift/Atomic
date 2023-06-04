package com.myth.atomic.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Reminders")
data class Reminder
(
    @PrimaryKey(autoGenerate = true)
    val title: String,
    val date: Long,
    val time: Long,
    val repeatReminder: Boolean
)