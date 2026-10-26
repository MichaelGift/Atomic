package com.myth.atomic.domain.model

import java.time.Instant

data class Task(
    val id: Long = 0,
    val goalId: Long,
    val title: String,
    val description: String = "",
    val dueAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
) {
    val isCompleted: Boolean get() = completedAt != null
}
