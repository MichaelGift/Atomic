package com.myth.atomic.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.myth.atomic.data.local.AtomicDatabase
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class RoomGoalRepositoryTest {
    private lateinit var db: AtomicDatabase
    private lateinit var goals: RoomGoalRepository
    private lateinit var tasks: RoomTaskRepository
    private val now = Instant.parse("2026-01-15T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AtomicDatabase::class.java,
        ).allowMainThreadQueries().build()
        goals = RoomGoalRepository(db.goalDao(), clock)
        tasks = RoomTaskRepository(db.taskDao(), clock)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun completingTasksUpdatesGoalProgress() = runTest {
        val goalId = goals.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val firstId = tasks.addTask(Task(goalId = goalId, title = "Buy shoes", createdAt = now))
        tasks.addTask(Task(goalId = goalId, title = "First 5k", createdAt = now))

        tasks.setTaskCompleted(firstId, completed = true)

        val progress = goals.observeGoalsWithProgress().first().single()
        assertEquals(0.5f, progress.progress)
        assertEquals(now, tasks.observeTasksForGoal(goalId).first().first { it.id == firstId }.completedAt)
    }

    @Test
    fun uncompletingGoalClearsTimestamp() = runTest {
        val goalId = goals.addGoal(Goal(title = "Run a 10k", createdAt = now))

        goals.setGoalCompleted(goalId, completed = true)
        assertEquals(now, goals.observeGoalDetails(goalId).first()!!.goal.completedAt)

        goals.setGoalCompleted(goalId, completed = false)
        assertNull(goals.observeGoalDetails(goalId).first()!!.goal.completedAt)
    }
}
