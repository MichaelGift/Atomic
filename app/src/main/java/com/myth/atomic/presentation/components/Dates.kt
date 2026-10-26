package com.myth.atomic.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.myth.atomic.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val mediumDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val fullDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

fun LocalDate.formatMedium(): String = format(mediumDate)

fun LocalDate.formatFull(): String = format(fullDate)

fun Instant.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate = atZone(zone).toLocalDate()

fun LocalDate.startOfDay(zone: ZoneId): Instant = atStartOfDay(zone).toInstant()

// The Material date picker works in UTC-midnight millis regardless of the device zone.
internal fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun pickerMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

/** "Overdue · Jan 3", "Today", "Tomorrow" or the date itself. */
@Composable
fun dueLabel(due: LocalDate, today: LocalDate): String = when {
    due.isBefore(today) -> stringResource(R.string.due_overdue, due.formatMedium())
    due == today -> stringResource(R.string.due_today)
    due == today.plusDays(1) -> stringResource(R.string.due_tomorrow)
    else -> stringResource(R.string.due_on, due.formatMedium())
}
