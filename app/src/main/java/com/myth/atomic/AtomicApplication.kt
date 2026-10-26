package com.myth.atomic

import android.app.Application
import android.os.Build
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.myth.atomic.di.ApplicationScope
import com.myth.atomic.reminders.RescheduleRemindersWorker
import com.myth.atomic.reminders.SystemReminderNotifier
import com.myth.atomic.widget.AtomicWidget
import com.myth.atomic.widget.AtomicWidgetReceiver
import com.myth.atomic.widget.WidgetDataSource
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AtomicApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var widgetDataSource: WidgetDataSource

    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        SystemReminderNotifier.createChannel(this)
        // Alarms are lost on force-stop as well as reboot; re-registering is cheap and idempotent.
        RescheduleRemindersWorker.enqueue(this)
        publishWidgetPreview()
        // All data changes happen in this process, so redraw the widget whenever its data moves.
        applicationScope.launch {
            widgetDataSource.observe().distinctUntilChanged().drop(1).collect {
                AtomicWidget().updateAll(this@AtomicApplication)
            }
        }
    }

    /**
     * Generated widget previews are rate-limited by the system, so publish them once per
     * app version rather than on every start.
     */
    private fun publishWidgetPreview() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val prefs = getSharedPreferences("widget", MODE_PRIVATE)
        val version = packageManager.getPackageInfo(packageName, 0).longVersionCode
        if (prefs.getLong(KEY_PREVIEW_VERSION, -1) == version) return
        applicationScope.launch {
            val result = GlanceAppWidgetManager(this@AtomicApplication).setWidgetPreviews(AtomicWidgetReceiver::class)
            if (result == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS) {
                prefs.edit { putLong(KEY_PREVIEW_VERSION, version) }
            }
        }
    }

    private companion object {
        const val KEY_PREVIEW_VERSION = "previewVersion"
    }
}
