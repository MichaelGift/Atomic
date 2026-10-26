package com.myth.atomic.domain.repository

import com.myth.atomic.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun observeEntries(): Flow<List<JournalEntry>>
    fun observeEntriesForGoal(goalId: Long): Flow<List<JournalEntry>>
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    suspend fun addEntry(entry: JournalEntry): Long
    suspend fun updateEntry(entry: JournalEntry)
    suspend fun deleteEntry(entryId: Long)
}
