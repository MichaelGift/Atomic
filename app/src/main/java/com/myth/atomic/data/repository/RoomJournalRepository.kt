package com.myth.atomic.data.repository

import com.myth.atomic.data.local.dao.JournalDao
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomJournalRepository @Inject constructor(
    private val journalDao: JournalDao,
) : JournalRepository {
    override fun observeEntries(): Flow<List<JournalEntry>> =
        journalDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeEntriesForGoal(goalId: Long): Flow<List<JournalEntry>> =
        journalDao.observeForGoal(goalId).map { rows -> rows.map { it.toDomain() } }

    override fun searchEntries(query: String): Flow<List<JournalEntry>> =
        journalDao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getEntry(entryId: Long): JournalEntry? = journalDao.getById(entryId)?.toDomain()

    override suspend fun addEntry(entry: JournalEntry): Long = journalDao.insert(entry.toEntity())

    override suspend fun updateEntry(entry: JournalEntry) = journalDao.update(entry.toEntity())

    override suspend fun deleteEntry(entryId: Long) = journalDao.deleteById(entryId)
}
