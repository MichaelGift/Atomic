package com.myth.atomic.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.myth.atomic.model.Journal

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal)

    @Delete
    suspend fun deleteJournal(journal: Journal)

    @Update
    suspend fun updateJournal(journal: Journal)

    @Query("SELECT * FROM Journals ORDER BY id DESC")
    fun getAllJournals(): LiveData<List<Journal>>

    @Query("SELECT * FROM Journals WHERE title LIKE '%' || :query || '%' " +
            "OR description LIKE '%'|| :query || '%'")
    fun searchJournals(query: String): LiveData<List<Journal>>
}