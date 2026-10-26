package com.myth.atomic.presentation.journal.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.JournalRepository
import com.myth.atomic.presentation.navigation.JournalEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class JournalEditorUiState(
    val isNew: Boolean,
    val isLoading: Boolean = !isNew,
    val title: String = "",
    val body: String = "",
    val goalId: Long? = null,
    val goals: List<Goal> = emptyList(),
    /** Set once the entry is saved or deleted; the screen should close. */
    val isDone: Boolean = false,
) {
    val canSave: Boolean get() = !isLoading && (title.isNotBlank() || body.isNotBlank())
}

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val journalRepository: JournalRepository,
    goalRepository: GoalRepository,
    private val clock: Clock,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<JournalEditorRoute>()
    private var existing: JournalEntry? = null

    // Snapshot state rather than StateFlow so text fields update synchronously while typing.
    var uiState by mutableStateOf(
        JournalEditorUiState(isNew = route.entryId == 0L, goalId = route.goalId.takeIf { it != 0L }),
    )
        private set

    init {
        if (route.entryId != 0L) {
            viewModelScope.launch {
                val entry = journalRepository.getEntry(route.entryId)
                existing = entry
                uiState = if (entry == null) {
                    uiState.copy(isLoading = false, isDone = true)
                } else {
                    uiState.copy(isLoading = false, title = entry.title, body = entry.body, goalId = entry.goalId)
                }
            }
        }
        viewModelScope.launch {
            goalRepository.observeGoalsWithProgress().collect { goals ->
                uiState = uiState.copy(goals = goals.map { it.goal })
            }
        }
    }

    fun onTitleChange(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun onBodyChange(body: String) {
        uiState = uiState.copy(body = body)
    }

    fun onGoalChange(goalId: Long?) {
        uiState = uiState.copy(goalId = goalId)
    }

    fun save() {
        val state = uiState
        if (!state.canSave) return
        viewModelScope.launch {
            val current = existing
            if (current == null) {
                journalRepository.addEntry(
                    JournalEntry(title = state.title.trim(), body = state.body.trim(), createdAt = clock.instant(), goalId = state.goalId),
                )
            } else {
                journalRepository.updateEntry(current.copy(title = state.title.trim(), body = state.body.trim(), goalId = state.goalId))
            }
            uiState = uiState.copy(isDone = true)
        }
    }

    fun delete() {
        val current = existing ?: return
        viewModelScope.launch {
            journalRepository.deleteEntry(current.id)
            uiState = uiState.copy(isDone = true)
        }
    }
}
