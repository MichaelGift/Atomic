package com.myth.atomic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myth.atomic.model.Goals
import com.myth.atomic.model.Journal
import com.myth.atomic.model.Reminder
import com.myth.atomic.model.Task

@Database(entities = [Goals::class, Reminder::class, Task::class, Journal::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getGoalDao(): GoalDao

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