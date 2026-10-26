package com.myth.atomic.domain.reminders

import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.model.RepeatInterval
import com.myth.atomic.domain.repository.GoalRepository
import com.myth.atomic.domain.repository.ReminderRepository
import com.myth.atomic.domain.repository.TaskRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Delivers a reminder at its trigger time; implemented with AlarmManager on Android. */
interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: Long)
}

data class ReminderNotification(
    val reminderId: Long,
    val title: String,
    /** The task or goal the reminder is about, if any. */
    val detail: String?,
    val goalId: Long?,
)

interface ReminderNotifier {
    fun show(notification: ReminderNotification)
}

/**
 * The first occurrence of a repeating reminder strictly after [after], keeping its local
 * time of day across daylight-saving changes; null for a one-off reminder.
 */
fun nextOccurrence(triggerAt: Instant, repeat: RepeatInterval, after: Instant, zone: ZoneId): Instant? {
    if (repeat == RepeatInterval.NONE) return null
    var next = triggerAt.atZone(zone)
    while (!next.toInstant().isAfter(after)) {
        next = when (repeat) {
            RepeatInterval.DAILY -> next.plusDays(1)
            RepeatInterval.WEEKLY -> next.plusWeeks(1)
            RepeatInterval.NONE -> error("unreachable")
        }
    }
    return next.toInstant()
}

class AddReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reminder: Reminder): Long {
        val id = reminderRepository.addReminder(reminder)
        scheduler.schedule(reminder.copy(id = id))
        return id
    }
}

class DeleteReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reminderId: Long) {
        scheduler.cancel(reminderId)
        reminderRepository.deleteReminder(reminderId)
    }
}

/** Runs when a reminder's alarm fires: notifies if still relevant, then schedules the next repeat. */
class HandleReminderAlarmUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    private val scheduler: ReminderScheduler,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) {
    suspend operator fun invoke(reminderId: Long) {
        // Gone if the reminder, or the goal or task it belonged to, was deleted.
        val reminder = reminderRepository.getReminder(reminderId) ?: return
        val task = reminder.taskId?.let { taskRepository.getTask(it) }
        val goal = (reminder.goalId ?: task?.goalId)?.let { goalRepository.getGoal(it) }

        if (task?.isCompleted != true && goal?.isCompleted != true) {
            // Reminder titles default to the goal's title; don't repeat it as the body.
            val detail = (task?.title ?: goal?.title)?.takeIf { it != reminder.title }
            notifier.show(ReminderNotification(reminder.id, reminder.title, detail, goal?.id))
        }

        val next = nextOccurrence(reminder.triggerAt, reminder.repeat, clock.instant(), clock.zone) ?: return
        val updated = reminder.copy(triggerAt = next)
        reminderRepository.updateReminder(updated)
        scheduler.schedule(updated)
    }
}

/**
 * Re-registers every alarm. The system drops alarms on reboot and force-stop, so this runs
 * at boot, after app updates and clock changes, and on each app start.
 */
class RescheduleRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        val now = clock.instant()
        for (reminder in reminderRepository.getAllReminders()) {
            if (reminder.triggerAt.isAfter(now)) {
                scheduler.schedule(reminder)
                continue
            }
            // Missed while the device was off: skip one-offs, move repeats to their next slot.
            val next = nextOccurrence(reminder.triggerAt, reminder.repeat, now, clock.zone) ?: continue
            val updated = reminder.copy(triggerAt = next)
            reminderRepository.updateReminder(updated)
            scheduler.schedule(updated)
        }
    }
}
