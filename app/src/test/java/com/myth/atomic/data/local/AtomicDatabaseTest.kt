package com.myth.atomic.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.myth.atomic.data.local.entity.GoalEntity
import com.myth.atomic.data.local.entity.JournalEntryEntity
import com.myth.atomic.data.local.entity.ReminderEntity
import com.myth.atomic.data.local.entity.TaskEntity
import com.myth.atomic.domain.model.RepeatInterval
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class AtomicDatabaseTest {
    private lateinit var db: AtomicDatabase
    private val now = Instant.parse("2026-01-15T09:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AtomicDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun goalProgressCountsTasks() = runTest {
        val goalId = db.goalDao().insert(goal("Run a 10k"))
        val emptyGoalId = db.goalDao().insert(goal("Learn Compose"))
        db.taskDao().insert(task(goalId, "Buy shoes", completedAt = now))
        db.taskDao().insert(task(goalId, "First 5k"))

        val rows = db.goalDao().observeWithTaskCounts().first().associateBy { it.goal.id }

        assertEquals(2, rows.getValue(goalId).totalTasks)
        assertEquals(1, rows.getValue(goalId).completedTasks)
        assertEquals(0, rows.getValue(emptyGoalId).totalTasks)
        assertEquals(0, rows.getValue(emptyGoalId).completedTasks)
    }

    @Test
    fun deletingGoalCascadesToTasksAndRemindersButKeepsJournal() = runTest {
        val goalId = db.goalDao().insert(goal("Run a 10k"))
        val taskId = db.taskDao().insert(task(goalId, "First 5k"))
        db.reminderDao().insert(
            ReminderEntity(title = "Run", triggerAt = now, repeat = RepeatInterval.DAILY, goalId = null, taskId = taskId)
        )
        db.journalDao().insert(
            JournalEntryEntity(title = "Day 1", body = "Legs hurt", createdAt = now, goalId = goalId, taskId = taskId)
        )

        db.goalDao().deleteById(goalId)

        assertTrue(db.taskDao().observeForGoal(goalId).first().isEmpty())
        assertTrue(db.reminderDao().observeAll().first().isEmpty())
        val entry = db.journalDao().observeAll().first().single()
        assertNull(entry.goalId)
        assertNull(entry.taskId)
    }

    @Test
    fun goalDetailsLoadsChildren() = runTest {
        val goalId = db.goalDao().insert(goal("Run a 10k"))
        db.taskDao().insert(task(goalId, "First 5k"))
        db.reminderDao().insert(
            ReminderEntity(title = "Check in", triggerAt = now, repeat = RepeatInterval.WEEKLY, goalId = goalId, taskId = null)
        )
        db.journalDao().insert(
            JournalEntryEntity(title = "Why", body = "Health", createdAt = now, goalId = goalId, taskId = null)
        )

        val details = db.goalDao().observeWithChildren(goalId).first()!!

        assertEquals(1, details.tasks.size)
        assertEquals(RepeatInterval.WEEKLY, details.reminders.single().repeat)
        assertEquals("Why", details.journalEntries.single().title)
    }

    @Test
    fun openTasksDueBeforeExcludesCompletedLaterAndUndated() = runTest {
        val goalId = db.goalDao().insert(goal("Run a 10k"))
        val endOfDay = now.plusSeconds(12 * 3600)
        db.taskDao().insert(task(goalId, "Overdue", dueAt = now.minusSeconds(86_400)))
        db.taskDao().insert(task(goalId, "Today", dueAt = now.plusSeconds(3600)))
        db.taskDao().insert(task(goalId, "Done", dueAt = now, completedAt = now))
        db.taskDao().insert(task(goalId, "Tomorrow", dueAt = endOfDay.plusSeconds(3600)))
        db.taskDao().insert(task(goalId, "Someday"))

        val due = db.taskDao().observeOpenDueBefore(endOfDay).first().map { it.title }

        assertEquals(listOf("Overdue", "Today"), due)
    }

    @Test
    fun searchMatchesSubstrings() = runTest {
        db.goalDao().insert(goal("Run a 10k", description = "Spring marathon prep"))
        db.goalDao().insert(goal("Read more"))

        assertEquals(listOf("Run a 10k"), db.goalDao().search("marathon").first().map { it.title })
        assertEquals(2, db.goalDao().search("R").first().size)
    }

    private fun goal(title: String, description: String = "") = GoalEntity(
        title = title,
        description = description,
        deadline = null,
        createdAt = now,
        completedAt = null,
    )

    private fun task(goalId: Long, title: String, dueAt: Instant? = null, completedAt: Instant? = null) = TaskEntity(
        goalId = goalId,
        title = title,
        description = "",
        dueAt = dueAt,
        createdAt = now,
        completedAt = completedAt,
    )
}
