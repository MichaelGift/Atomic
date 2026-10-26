package com.myth.atomic.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class JournalItem(val entry: JournalEntry, val goalTitle: String?, val goalColorIndex: Int = 0)

data class JournalUiState(
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val entries: List<JournalItem> = emptyList(),
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    journalRepository: JournalRepository,
    goalRepository: GoalRepository,
) : ViewModel() {
    // Kept apart from uiState so the search field updates synchronously while typing.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val entries = _query.flatMapLatest { query ->
        if (query.isBlank()) journalRepository.observeEntries() else journalRepository.searchEntries(query.trim())
    }

    val uiState: StateFlow<JournalUiState> = combine(
        entries,
        goalRepository.observeGoalsWithProgress(),
        _query,
    ) { entries, goals, query ->
        val goalsById = goals.associate { it.goal.id to it.goal }
        JournalUiState(
            isLoading = false,
            isSearching = query.isNotBlank(),
            entries = entries.map { entry ->
                val goal = entry.goalId?.let(goalsById::get)
                JournalItem(entry, goal?.title, goal?.colorIndex ?: 0)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
