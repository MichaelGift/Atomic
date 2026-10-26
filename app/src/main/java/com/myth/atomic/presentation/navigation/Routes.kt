package com.myth.atomic.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object TodayRoute

@Serializable
data object GoalsRoute

@Serializable
data object JournalRoute

@Serializable
data class GoalDetailRoute(val goalId: Long)

/** [entryId] 0 creates a new entry; [goalId] 0 means the entry is not linked to a goal. */
@Serializable
data class JournalEditorRoute(val entryId: Long = 0, val goalId: Long = 0)
