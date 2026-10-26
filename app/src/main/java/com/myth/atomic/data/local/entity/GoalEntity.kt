package com.myth.atomic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val deadline: Instant?,
    val createdAt: Instant,
    val completedAt: Instant?,
    @ColumnInfo(defaultValue = "0")
    val colorIndex: Int = 0,
)
