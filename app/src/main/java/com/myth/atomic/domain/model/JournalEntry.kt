package com.myth.atomic.domain.model

import java.time.Instant

/** A journal entry may reflect on a goal, a task, or neither (a free-form entry). */
data class JournalEntry(
    val id: Long = 0,
    val title: String,
    val body: String,
    val createdAt: Instant = Instant.now(),
    val goalId: Long? = null,
    val taskId: Long? = null,
)
