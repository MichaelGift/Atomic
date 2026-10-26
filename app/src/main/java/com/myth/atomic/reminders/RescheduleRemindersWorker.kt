package com.myth.atomic.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.myth.atomic.domain.reminders.RescheduleRemindersUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RescheduleRemindersWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val rescheduleReminders: RescheduleRemindersUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        rescheduleReminders()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "reschedule-reminders"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RescheduleRemindersWorker>().build(),
            )
        }
    }
}

/** Alarms do not survive reboots, and clock changes shift their meaning; re-register them. */
class SystemEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> RescheduleRemindersWorker.enqueue(context)
        }
    }
}
