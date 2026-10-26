package com.myth.atomic

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.myth.atomic.di.ApplicationScope
import com.myth.atomic.reminders.RescheduleRemindersWorker
import com.myth.atomic.reminders.SystemReminderNotifier
import com.myth.atomic.widget.AtomicWidget
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
        // All data changes happen in this process, so redraw the widget whenever its data moves.
        applicationScope.launch {
            widgetDataSource.observe().distinctUntilChanged().drop(1).collect {
                AtomicWidget().updateAll(this@AtomicApplication)
            }
        }
    }
}
