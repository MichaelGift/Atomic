package com.myth.atomic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.myth.atomic.data.local.entity.GoalEntity
import com.myth.atomic.data.local.entity.GoalWithChildren
import com.myth.atomic.data.local.entity.GoalWithTaskCounts
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("UPDATE goals SET completedAt = :completedAt WHERE id = :goalId")
    suspend fun setCompletedAt(goalId: Long, completedAt: Instant?)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteById(goalId: Long)

    // Open goals first, newest first within each group.
    @Query(
        """
        SELECT goals.*,
            COUNT(tasks.id) AS totalTasks,
            COUNT(tasks.completedAt) AS completedTasks
        FROM goals
        LEFT JOIN tasks ON tasks.goalId = goals.id
        GROUP BY goals.id
        ORDER BY goals.completedAt IS NOT NULL, goals.createdAt DESC
        """
    )
    fun observeWithTaskCounts(): Flow<List<GoalWithTaskCounts>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun observeWithChildren(goalId: Long): Flow<GoalWithChildren?>

    @Query(
        """
        SELECT * FROM goals
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<GoalEntity>>
}
