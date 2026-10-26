package com.myth.atomic.reminders

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myth.atomic.MainActivity
import com.myth.atomic.R
import com.myth.atomic.domain.reminders.ReminderNotification
import com.myth.atomic.domain.reminders.ReminderNotifier
import com.myth.atomic.presentation.navigation.GOAL_DEEP_LINK_BASE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemReminderNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ReminderNotifier {
    private val notificationManager = NotificationManagerCompat.from(context)

    // Permission is checked through areNotificationsEnabled(), which lint does not recognize.
    @SuppressLint("MissingPermission")
    override fun show(notification: ReminderNotification) {
        // False when the user declined POST_NOTIFICATIONS (Android 13+) or turned notifications off.
        if (!notificationManager.areNotificationsEnabled()) return

        val openIntent = if (notification.goalId != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse("$GOAL_DEEP_LINK_BASE/${notification.goalId}"), context, MainActivity::class.java)
        } else {
            Intent(context, MainActivity::class.java)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val built = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.detail)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    notification.reminderId.toInt(),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
        try {
            notificationManager.notify(notification.reminderId.toInt(), built)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call.
        }
    }

    companion object {
        const val CHANNEL_ID = "reminders"

        fun createChannel(context: Context) {
            NotificationManagerCompat.from(context).createNotificationChannel(
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(context.getString(R.string.notification_channel_reminders))
                    .setDescription(context.getString(R.string.notification_channel_reminders_description))
                    .build(),
            )
        }
    }
}
