package com.myth.atomic.presentation.goaldetail

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.myth.atomic.R
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.model.RepeatInterval
import com.myth.atomic.presentation.components.DatePickerButton
import com.myth.atomic.presentation.components.formatMedium
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private val shortTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
fun ReminderRow(reminder: Reminder, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val zone = ZoneId.systemDefault()
    val at = reminder.triggerAt.atZone(zone)
    val time = at.toLocalTime().format(shortTime)
    val schedule = when (reminder.repeat) {
        RepeatInterval.NONE -> stringResource(R.string.reminder_once, at.toLocalDate().formatMedium(), time)
        RepeatInterval.DAILY -> stringResource(R.string.reminder_daily, time)
        RepeatInterval.WEEKLY -> stringResource(
            R.string.reminder_weekly,
            at.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
            time,
        )
    }
    val isPast = reminder.repeat == RepeatInterval.NONE && reminder.triggerAt.isBefore(Instant.now())

    ListItem(
        modifier = modifier,
        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
        headlineContent = { Text(reminder.title) },
        supportingContent = {
            Text(
                if (isPast) stringResource(R.string.reminder_past, schedule) else schedule,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.reminder_delete))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    defaultTitle: String,
    onDismiss: () -> Unit,
    onSave: (title: String, date: LocalDate, time: LocalTime, repeat: RepeatInterval) -> Unit,
) {
    // Default to the next full hour.
    val initial = remember { LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0) }
    var title by rememberSaveable { mutableStateOf(defaultTitle) }
    var date by rememberSaveable { mutableStateOf(initial.toLocalDate()) }
    var time by rememberSaveable { mutableStateOf(initial.toLocalTime()) }
    var repeat by rememberSaveable { mutableStateOf(RepeatInterval.NONE) }
    var pickingTime by rememberSaveable { mutableStateOf(false) }

    // A one-off reminder in the past would never fire.
    val inPast = repeat == RepeatInterval.NONE && !date.atTime(time).isAfter(LocalDateTime.now())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_title_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DatePickerButton(date = date, emptyLabel = "", onDateChange = { if (it != null) date = it }, clearable = false)
                    AssistChip(onClick = { pickingTime = true }, label = { Text(time.format(shortTime)) })
                }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = RepeatInterval.entries
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = repeat == option,
                            onClick = { repeat = option },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        ) {
                            Text(
                                stringResource(
                                    when (option) {
                                        RepeatInterval.NONE -> R.string.repeat_none
                                        RepeatInterval.DAILY -> R.string.repeat_daily
                                        RepeatInterval.WEEKLY -> R.string.repeat_weekly
                                    },
                                ),
                            )
                        }
                    }
                }
                if (inPast) {
                    Text(
                        stringResource(R.string.reminder_in_past),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, date, time, repeat) }, enabled = title.isNotBlank() && !inPast) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )

    if (pickingTime) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = DateFormat.is24HourFormat(LocalContext.current),
        )
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    pickingTime = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { pickingTime = false }) { Text(stringResource(android.R.string.cancel)) }
            },
            text = { TimePicker(state = state) },
        )
    }
}
