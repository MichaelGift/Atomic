package com.myth.atomic.domain.reminders

import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.model.RepeatInterval
import com.myth.atomic.domain.model.Task
import com.myth.atomic.testing.FakeAtomicRepository
import com.myth.atomic.testing.FakeReminderNotifier
import com.myth.atomic.testing.FakeReminderScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class RemindersTest {
    private val now = Instant.parse("2026-03-14T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val repository = FakeAtomicRepository(clock)
    private val scheduler = FakeReminderScheduler()
    private val notifier = FakeReminderNotifier()

    private val handleAlarm = HandleReminderAlarmUseCase(repository, repository, repository, scheduler, notifier, clock)
    private val reschedule = RescheduleRemindersUseCase(repository, scheduler, clock)

    @Test
    fun nextOccurrenceSkipsOneOffs() {
        assertNull(nextOccurrence(now, RepeatInterval.NONE, now, ZoneOffset.UTC))
    }

    @Test
    fun nextOccurrenceCatchesUpPastMissedDays() {
        val threeDaysAgo = now.minus(Duration.ofDays(3))

        assertEquals(now.plus(Duration.ofDays(1)), nextOccurrence(threeDaysAgo, RepeatInterval.DAILY, now, ZoneOffset.UTC))
        assertEquals(threeDaysAgo.plus(Duration.ofDays(7)), nextOccurrence(threeDaysAgo, RepeatInterval.WEEKLY, now, ZoneOffset.UTC))
    }

    @Test
    fun nextOccurrenceKeepsLocalTimeAcrossDaylightSaving() {
        // US clocks spring forward on 2026-03-08; a 7:00 reminder must stay at 7:00 local time.
        val zone = ZoneId.of("America/New_York")
        val before = Instant.parse("2026-03-07T12:00:00Z") // 07:00 EST

        val next = nextOccurrence(before, RepeatInterval.DAILY, before, zone)!!

        assertEquals(7, next.atZone(zone).hour)
        assertEquals(Duration.ofHours(23), Duration.between(before, next))
    }

    @Test
    fun alarmNotifiesAndSchedulesNextRepeat() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val id = repository.addReminder(Reminder(title = "Go for a run", triggerAt = now, repeat = RepeatInterval.DAILY, goalId = goalId))

        handleAlarm(id)

        assertEquals(listOf(ReminderNotification(id, "Go for a run", "Run a 10k", goalId)), notifier.shown)
        val tomorrow = now.plus(Duration.ofDays(1))
        assertEquals(tomorrow, repository.reminders.value.single().triggerAt)
        assertEquals(mapOf(id to tomorrow), scheduler.scheduled)
    }

    @Test
    fun alarmForCompletedTaskIsSilent() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val taskId = repository.addTask(Task(goalId = goalId, title = "Buy shoes", createdAt = now, completedAt = now))
        val id = repository.addReminder(Reminder(title = "Shoes", triggerAt = now, taskId = taskId))

        handleAlarm(id)

        assertTrue(notifier.shown.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun alarmForDeletedReminderDoesNothing() = runTest {
        handleAlarm(42)

        assertTrue(notifier.shown.isEmpty())
    }

    @Test
    fun rescheduleRestoresFutureAndRepeatingButSkipsMissedOneOffs() = runTest {
        val future = repository.addReminder(Reminder(title = "Later", triggerAt = now.plus(Duration.ofHours(2))))
        val missedOnce = repository.addReminder(Reminder(title = "Missed", triggerAt = now.minus(Duration.ofHours(2))))
        val missedDaily = repository.addReminder(
            Reminder(title = "Daily", triggerAt = now.minus(Duration.ofHours(2)), repeat = RepeatInterval.DAILY),
        )

        reschedule()

        assertEquals(now.plus(Duration.ofHours(2)), scheduler.scheduled[future])
        assertNull(scheduler.scheduled[missedOnce])
        assertEquals(now.plus(Duration.ofHours(22)), scheduler.scheduled[missedDaily])
        assertEquals(now.plus(Duration.ofHours(22)), repository.getReminder(missedDaily)!!.triggerAt)
    }
}
