package com.myth.atomic.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myth.atomic.di.ApplicationScope
import com.myth.atomic.domain.reminders.HandleReminderAlarmUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var handleReminderAlarm: HandleReminderAlarmUseCase

    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId == -1L) return
        // Keeps the process alive while the database work finishes.
        val pendingResult = goAsync()
        scope.launch {
            try {
                handleReminderAlarm(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminderId"
    }
}
