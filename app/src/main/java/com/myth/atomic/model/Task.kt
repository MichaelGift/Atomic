package com.myth.atomic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val taskTitle: String,
    val taskDescription: String,
    val taskReminders: List<Reminder>,
    val isTaskCompleted: Boolean,
    val taskJournal: Journal,
    val goalID:Int
)
