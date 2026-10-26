package com.myth.atomic.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.reminders.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ReminderScheduler {
    private val alarmManager = context.getSystemService<AlarmManager>()!!

    override fun schedule(reminder: Reminder) {
        val pendingIntent = pendingIntent(reminder.id, PendingIntent.FLAG_UPDATE_CURRENT)!!
        val triggerAt = reminder.triggerAt.toEpochMilli()
        // Exact alarms need the user's permission on Android 12+; without it the system may
        // deliver the reminder a few minutes late, which is acceptable for this app.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    override fun cancel(reminderId: Long) {
        pendingIntent(reminderId, PendingIntent.FLAG_NO_CREATE)?.let(alarmManager::cancel)
    }

    private fun pendingIntent(reminderId: Long, flags: Int): PendingIntent? = PendingIntent.getBroadcast(
        context,
        reminderId.toInt(),
        Intent(context, ReminderAlarmReceiver::class.java).putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminderId),
        flags or PendingIntent.FLAG_IMMUTABLE,
    )
}
