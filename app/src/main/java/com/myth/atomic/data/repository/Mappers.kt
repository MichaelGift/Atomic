package com.myth.atomic.data.repository

import com.myth.atomic.data.local.entity.GoalEntity
import com.myth.atomic.data.local.entity.GoalWithChildren
import com.myth.atomic.data.local.entity.GoalWithTaskCounts
import com.myth.atomic.data.local.entity.JournalEntryEntity
import com.myth.atomic.data.local.entity.ReminderEntity
import com.myth.atomic.data.local.entity.TaskEntity
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.GoalWithProgress
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.model.Task

internal fun GoalEntity.toDomain() = Goal(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    createdAt = createdAt,
    completedAt = completedAt,
)

internal fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    createdAt = createdAt,
    completedAt = completedAt,
)

internal fun GoalWithTaskCounts.toDomain() = GoalWithProgress(
    goal = goal.toDomain(),
    totalTasks = totalTasks,
    completedTasks = completedTasks,
)

internal fun GoalWithChildren.toDomain() = GoalDetails(
    goal = goal.toDomain(),
    tasks = tasks.map { it.toDomain() },
    reminders = reminders.map { it.toDomain() },
    journalEntries = journalEntries.map { it.toDomain() }.sortedByDescending { it.createdAt },
)

internal fun TaskEntity.toDomain() = Task(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    dueAt = dueAt,
    createdAt = createdAt,
    completedAt = completedAt,
)

internal fun Task.toEntity() = TaskEntity(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    dueAt = dueAt,
    createdAt = createdAt,
    completedAt = completedAt,
)

internal fun ReminderEntity.toDomain() = Reminder(
    id = id,
    title = title,
    triggerAt = triggerAt,
    repeat = repeat,
    goalId = goalId,
    taskId = taskId,
)

internal fun Reminder.toEntity() = ReminderEntity(
    id = id,
    title = title,
    triggerAt = triggerAt,
    repeat = repeat,
    goalId = goalId,
    taskId = taskId,
)

internal fun JournalEntryEntity.toDomain() = JournalEntry(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    goalId = goalId,
    taskId = taskId,
)

internal fun JournalEntry.toEntity() = JournalEntryEntity(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    goalId = goalId,
    taskId = taskId,
)
