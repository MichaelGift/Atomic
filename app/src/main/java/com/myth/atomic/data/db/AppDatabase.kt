package com.myth.atomic.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myth.atomic.data.model.Goals
import com.myth.atomic.data.model.Journal
import com.myth.atomic.data.model.Reminder
import com.myth.atomic.data.model.Task

@Database(entities = [Goals::class, Reminder::class, Task::class, Journal::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getGoalDao(): GoalDao
    abstract fun getJournalDao(): JournalDao

    companion object{
        @Volatile
        private var instance : AppDatabase? = null
        private val LOCK  =Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java, "Atomic_db"
        ).build()
    }
}