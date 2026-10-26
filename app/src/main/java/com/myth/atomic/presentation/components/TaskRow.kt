package com.myth.atomic.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myth.atomic.R
import com.myth.atomic.domain.model.Task
import com.myth.atomic.presentation.theme.AtomicTheme
import java.time.LocalDate

/**
 * One task: an animated check, the title, and its goal and due date as tags.
 * When [onDelete] is set the row can be swiped away.
 */
@Composable
fun TaskRow(
    task: Task,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    goalTitle: String? = null,
    goalColor: Color? = null,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    if (onDelete == null) {
        TaskRowContent(task, today, onCheckedChange, modifier, goalTitle, goalColor, onClick)
        return
    }
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = { onDelete() },
        backgroundContent = {
            val color by animateColorAsState(
                if (state.targetValue == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.error else Color.Transparent,
                label = "swipe",
            )
            Box(
                Modifier.fillMaxSize().background(color, MaterialTheme.shapes.medium).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete_task), tint = MaterialTheme.colorScheme.onError)
            }
        },
    ) {
        TaskRowContent(task, today, onCheckedChange, Modifier, goalTitle, goalColor, onClick)
    }
}

@Composable
private fun TaskRowContent(
    task: Task,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    goalTitle: String?,
    goalColor: Color?,
    onClick: (() -> Unit)?,
) {
    val dueDate = task.dueAt?.toLocalDate()
    val overdue = !task.isCompleted && dueDate != null && dueDate.isBefore(today)
    // A finished task is never overdue; just show when it was due.
    val dueText = dueDate?.let { if (task.isCompleted) stringResource(R.string.due_on, it.formatMedium()) else dueLabel(it, today) }
    val titleColor by animateColorAsState(
        if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        label = "title",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckButton(checked = task.isCompleted, onCheckedChange = onCheckedChange, color = goalColor ?: AtomicTheme.colors.accent)
        Column(Modifier.weight(1f).padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (goalTitle != null || dueText != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (goalTitle != null) Tag(goalTitle, dotColor = goalColor, modifier = Modifier.weight(1f, fill = false))
                    if (dueText != null) {
                        Tag(
                            dueText,
                            contentColor = if (overdue) AtomicTheme.colors.ember else MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = if (overdue) AtomicTheme.colors.ember.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                    }
                }
            }
        }
    }
}
