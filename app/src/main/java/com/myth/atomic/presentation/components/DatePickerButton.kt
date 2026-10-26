package com.myth.atomic.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.myth.atomic.R
import java.time.LocalDate

/** A chip showing [date] (or [emptyLabel]) that opens a date picker; the trailing button clears it. */
@Composable
fun DatePickerButton(
    date: LocalDate?,
    emptyLabel: String,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AssistChip(
            onClick = { showPicker = true },
            label = { Text(date?.formatMedium() ?: emptyLabel) },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        )
        if (date != null) {
            IconButton(onClick = { onDateChange(null) }) {
                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_date))
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date?.toPickerMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateChange(pickerMillisToLocalDate(it)) }
                        showPicker = false
                    },
                ) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
