package com.myth.atomic.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myth.atomic.data.local.dao.GoalDao
import com.myth.atomic.data.local.dao.JournalDao
import com.myth.atomic.data.local.dao.ReminderDao
import com.myth.atomic.data.local.dao.TaskDao
import com.myth.atomic.data.local.entity.GoalEntity
import com.myth.atomic.data.local.entity.JournalEntryEntity
import com.myth.atomic.data.local.entity.ReminderEntity
import com.myth.atomic.data.local.entity.TaskEntity

@Database(
    entities = [GoalEntity::class, TaskEntity::class, ReminderEntity::class, JournalEntryEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // 2: goals gain a color.
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(Converters::class)
abstract class AtomicDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun journalDao(): JournalDao

    companion object {
        const val NAME = "atomic.db"
    }
}
