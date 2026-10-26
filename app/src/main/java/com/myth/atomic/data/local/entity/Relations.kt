package com.myth.atomic.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithTaskCounts(
    @Embedded val goal: GoalEntity,
    val totalTasks: Int,
    val completedTasks: Int,
)

data class GoalWithChildren(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val tasks: List<TaskEntity>,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val reminders: List<ReminderEntity>,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val journalEntries: List<JournalEntryEntity>,
)
