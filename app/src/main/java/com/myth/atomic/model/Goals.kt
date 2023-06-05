package com.myth.atomic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Goals")
data class Goals(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val goalName: String,
    val goalDescription:String,
    val goalDeadline: Long,
    val goalTasks: List<Task>,
    val goalReminder: Reminder,
    val isGoalCompleted: Boolean,
    val goalJournal: Journal
)
