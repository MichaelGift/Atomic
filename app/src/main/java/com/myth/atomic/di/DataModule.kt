package com.myth.atomic.di

import android.content.Context
import androidx.room.Room
import com.myth.atomic.data.local.AtomicDatabase
import com.myth.atomic.data.repository.RoomGoalRepository
import com.myth.atomic.data.repository.RoomJournalRepository
import com.myth.atomic.data.repository.RoomReminderRepository
import com.myth.atomic.data.repository.RoomTaskRepository
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.JournalRepository
import com.myth.atomic.domain.repository.ReminderRepository
import com.myth.atomic.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AtomicDatabase =
        Room.databaseBuilder(context, AtomicDatabase::class.java, AtomicDatabase.NAME).build()

    @Provides
    fun provideGoalDao(db: AtomicDatabase) = db.goalDao()

    @Provides
    fun provideTaskDao(db: AtomicDatabase) = db.taskDao()

    @Provides
    fun provideReminderDao(db: AtomicDatabase) = db.reminderDao()

    @Provides
    fun provideJournalDao(db: AtomicDatabase) = db.journalDao()

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindGoalRepository(impl: RoomGoalRepository): GoalRepository

    @Binds
    abstract fun bindTaskRepository(impl: RoomTaskRepository): TaskRepository

    @Binds
    abstract fun bindReminderRepository(impl: RoomReminderRepository): ReminderRepository

    @Binds
    abstract fun bindJournalRepository(impl: RoomJournalRepository): JournalRepository
}
