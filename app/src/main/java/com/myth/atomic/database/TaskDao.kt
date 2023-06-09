package com.myth.atomic.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.myth.atomic.model.Task

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM Tasks WHERE goalID LIKE :query")
    fun searchTasks(query:Int):LiveData<List<Task>>

    @Query("SELECT * FROM Tasks ORDER BY id DESC")
    fun getAllTasks():LiveData<List<Task>>
}