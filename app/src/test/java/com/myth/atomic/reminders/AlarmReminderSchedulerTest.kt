package com.myth.atomic.reminders

import android.app.AlarmManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import com.myth.atomic.domain.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class AlarmReminderSchedulerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val alarmManager = shadowOf(context.getSystemService<AlarmManager>())
    private val scheduler = AlarmReminderScheduler(context)

    @Test
    fun schedulesWakeupAlarmForReminderAndCancelsIt() {
        val at = Instant.parse("2026-03-15T07:30:00Z")

        scheduler.schedule(Reminder(id = 5, title = "Run", triggerAt = at))

        val alarm = alarmManager.peekNextScheduledAlarm()!!
        assertEquals(AlarmManager.RTC_WAKEUP, alarm.getType())
        assertEquals(at.toEpochMilli(), alarm.getTriggerAtMs())

        scheduler.cancel(5)
        assertNull(alarmManager.peekNextScheduledAlarm())
    }

    @Test
    fun reschedulingReplacesTheExistingAlarm() {
        scheduler.schedule(Reminder(id = 5, title = "Run", triggerAt = Instant.parse("2026-03-15T07:30:00Z")))
        scheduler.schedule(Reminder(id = 5, title = "Run", triggerAt = Instant.parse("2026-03-16T07:30:00Z")))

        assertEquals(1, alarmManager.scheduledAlarms.size)
    }
}
