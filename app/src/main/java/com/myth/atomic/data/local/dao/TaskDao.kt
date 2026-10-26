package com.myth.atomic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.myth.atomic.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET completedAt = :completedAt WHERE id = :taskId")
    suspend fun setCompletedAt(taskId: Long, completedAt: Instant?)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: Long): TaskEntity?

    @Query("SELECT completedAt FROM tasks WHERE completedAt IS NOT NULL")
    fun observeCompletionTimes(): Flow<List<Instant>>

    // Open tasks first, then by due date with undated tasks last.
    @Query(
        """
        SELECT * FROM tasks WHERE goalId = :goalId
        ORDER BY completedAt IS NOT NULL, dueAt IS NULL, dueAt, createdAt
        """
    )
    fun observeForGoal(goalId: Long): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE completedAt IS NULL AND dueAt IS NOT NULL AND dueAt < :end
        ORDER BY dueAt
        """
    )
    fun observeOpenDueBefore(end: Instant): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<TaskEntity>>
}
