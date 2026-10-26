package com.myth.atomic.domain.model

import java.time.Instant

/** A reminder may belong to a goal, a task, or neither (a standalone reminder). */
data class Reminder(
    val id: Long = 0,
    val title: String,
    val triggerAt: Instant,
    val repeat: RepeatInterval = RepeatInterval.NONE,
    val goalId: Long? = null,
    val taskId: Long? = null,
)

enum class RepeatInterval { NONE, DAILY, WEEKLY }
