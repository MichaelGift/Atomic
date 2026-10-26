package com.myth.atomic.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.myth.atomic.R
import com.myth.atomic.domain.model.Task
import java.time.LocalDate

@Composable
fun TaskRow(
    task: Task,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    goalTitle: String? = null,
    onDelete: (() -> Unit)? = null,
) {
    val dueDate = task.dueAt?.toLocalDate()
    // A finished task is never overdue; just show when it was due.
    val dueText = dueDate?.let { if (task.isCompleted) stringResource(R.string.due_on, it.formatMedium()) else dueLabel(it, today) }
    val supporting = listOfNotNull(goalTitle, dueText).joinToString(" · ")
    val overdue = !task.isCompleted && dueDate != null && dueDate.isBefore(today)

    ListItem(
        modifier = modifier,
        leadingContent = { Checkbox(checked = task.isCompleted, onCheckedChange = onCheckedChange) },
        headlineContent = {
            Text(
                task.title,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = if (supporting.isNotEmpty()) {
            {
                Text(
                    supporting,
                    color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        },
        trailingContent = onDelete?.let {
            {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete_task))
                }
            }
        },
    )
}
