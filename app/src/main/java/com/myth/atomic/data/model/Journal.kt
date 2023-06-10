package com.myth.atomic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Journals")
data class Journal(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title:String,
    val Description: String,
    val date: Long,
    val isTaskJournal: Boolean,
    val isGoalJournal: Boolean
)
