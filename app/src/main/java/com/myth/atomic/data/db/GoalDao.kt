package com.myth.atomic.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.myth.atomic.data.model.Goals

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goals: Goals)

    @Update
    suspend fun updateGoal(goals: Goals)

    @Delete
    suspend fun deleteGoal(goals: Goals)

    @Query("SELECT * FROM Goals ORDER BY id DESC")
    fun getAllGoals(): LiveData<List<Goals>>

    @Query("SELECT * FROM Goals WHERE goalDescription LIKE '%' || :query || '%' OR goalName LIKE '%' || :query || '%'")
    fun searchGoals(query: String): LiveData<List<Goals>>
}