package com.myth.atomic.domain.model

import java.time.Instant

data class Goal(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val deadline: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
) {
    val isCompleted: Boolean get() = completedAt != null
}

data class GoalWithProgress(
    val goal: Goal,
    val totalTasks: Int,
    val completedTasks: Int,
) {
    /** Fraction of tasks completed, in 0..1. A goal with no tasks has no progress yet. */
    val progress: Float get() = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks
}

data class GoalDetails(
    val goal: Goal,
    val tasks: List<Task>,
    val reminders: List<Reminder>,
    val journalEntries: List<JournalEntry>,
)
