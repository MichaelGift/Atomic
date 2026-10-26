package com.myth.atomic.presentation.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalWithProgress
import com.myth.atomic.presentation.components.AtomicCard
import com.myth.atomic.presentation.components.EmptyState
import com.myth.atomic.presentation.components.FloatingBarClearance
import com.myth.atomic.presentation.components.ProgressRing
import com.myth.atomic.presentation.components.SectionHeader
import com.myth.atomic.presentation.components.Tag
import com.myth.atomic.presentation.components.startOfDay
import com.myth.atomic.presentation.components.toLocalDate
import com.myth.atomic.presentation.theme.AtomicTheme
import com.myth.atomic.presentation.theme.GoalPalette
import com.myth.atomic.presentation.theme.goalColor
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun GoalsScreen(
    onOpenGoal: (Long) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditor by rememberSaveable { mutableStateOf(false) }

    GoalsContent(state = state, onOpenGoal = onOpenGoal, onAddGoal = { showEditor = true })

    if (showEditor) {
        GoalEditorSheet(
            initialColorIndex = state.nextColorIndex % GoalPalette.size,
            onDismiss = { showEditor = false },
            onSave = { title, description, deadline, color ->
                viewModel.addGoal(title, description, deadline, color)
                showEditor = false
            },
        )
    }
}

@Composable
fun GoalsContent(
    state: GoalsUiState,
    onOpenGoal: (Long) -> Unit,
    onAddGoal: () -> Unit,
) {
    val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = navBar + FloatingBarClearance + 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "header") {
                    Column(Modifier.padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.nav_goals), style = MaterialTheme.typography.headlineLarge)
                        Text(
                            stringResource(R.string.goals_summary, state.active.size, state.completed.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (state.isEmpty) {
                    item(key = "empty") {
                        EmptyState(
                            title = stringResource(R.string.goals_empty_title),
                            message = stringResource(R.string.goals_empty_message),
                        )
                    }
                }
                items(state.active, key = { it.goal.id }) { goal ->
                    GoalCard(goal, state.today, onClick = { onOpenGoal(goal.goal.id) }, modifier = Modifier.animateItem())
                }
                if (state.completed.isNotEmpty()) {
                    item(key = "completed-header") {
                        SectionHeader(
                            stringResource(R.string.goals_section_completed),
                            Modifier.padding(top = 16.dp),
                            count = state.completed.size,
                        )
                    }
                    items(state.completed, key = { it.goal.id }) { goal ->
                        GoalCard(
                            goal,
                            state.today,
                            onClick = { onOpenGoal(goal.goal.id) },
                            modifier = Modifier.animateItem().alpha(0.6f),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddGoal,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = navBar + FloatingBarClearance),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.goal_new))
        }
    }
}

@Composable
private fun GoalCard(item: GoalWithProgress, today: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = goalColor(item.goal.colorIndex)
    AtomicCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = item.progress, size = 68.dp, strokeWidth = 7.dp, color = color) {
                Text("${(item.progress * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.goal.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.goal.description.isNotBlank()) {
                    Text(
                        item.goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(taskProgressLabel(item.completedTasks, item.totalTasks))
                    if (!item.goal.isCompleted) item.goal.deadline?.let { DeadlineTag(it.toLocalDate(), today) }
                }
            }
        }
    }
}

/** "12 days left", "Due today" or "3 days over", with urgency in ember. */
@Composable
fun DeadlineTag(deadline: LocalDate, today: LocalDate, modifier: Modifier = Modifier) {
    val days = ChronoUnit.DAYS.between(today, deadline).toInt()
    val text = when {
        days > 0 -> pluralStringResource(R.plurals.goal_days_left, days, days)
        days == 0 -> stringResource(R.string.goal_due_today)
        else -> pluralStringResource(R.plurals.goal_days_over, -days, -days)
    }
    val urgent = days <= 3
    Tag(
        text,
        modifier = modifier,
        contentColor = if (urgent) AtomicTheme.colors.ember else MaterialTheme.colorScheme.onSurfaceVariant,
        containerColor = if (urgent) AtomicTheme.colors.ember.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
fun taskProgressLabel(completed: Int, total: Int): String =
    if (total == 0) stringResource(R.string.goal_no_tasks)
    else pluralStringResource(R.plurals.goal_tasks_progress, total, completed, total)

@Preview
@Composable
private fun GoalsContentPreview() {
    val today = LocalDate.of(2026, 3, 14)
    val zone = ZoneId.systemDefault()
    val now = today.startOfDay(zone)
    AtomicTheme {
        GoalsContent(
            state = GoalsUiState(
                today = today,
                isLoading = false,
                active = listOf(
                    GoalWithProgress(Goal(1, "Run a 10k", "Spring race in May", today.plusDays(60).startOfDay(zone), now), 6, 3),
                    GoalWithProgress(Goal(2, "Read 12 books", "One a month", today.plusDays(2).startOfDay(zone), now, colorIndex = 1), 12, 9),
                ),
                completed = listOf(GoalWithProgress(Goal(3, "Learn Compose", createdAt = now, completedAt = now, colorIndex = 3), 5, 5)),
            ),
            onOpenGoal = {},
            onAddGoal = {},
        )
    }
}
