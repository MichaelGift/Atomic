package com.myth.atomic.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.Task
import com.myth.atomic.domain.stats.StreakCalculator
import com.myth.atomic.presentation.components.ActivityHeatmap
import com.myth.atomic.presentation.components.AtomicCard
import com.myth.atomic.presentation.components.DatePickerButton
import com.myth.atomic.presentation.components.EditorSheet
import com.myth.atomic.presentation.components.EmptyState
import com.myth.atomic.presentation.components.FieldText
import com.myth.atomic.presentation.components.FloatingBarClearance
import com.myth.atomic.presentation.components.ProgressRing
import com.myth.atomic.presentation.components.SectionHeader
import com.myth.atomic.presentation.components.TaskRow
import com.myth.atomic.presentation.components.WeekStrip
import com.myth.atomic.presentation.components.startOfDay
import com.myth.atomic.presentation.theme.AtomicTheme
import com.myth.atomic.presentation.theme.goalColor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    onOpenGoal: (Long) -> Unit,
    onOpenGoals: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshDate()
        onPauseOrDispose {}
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val completedMessage = stringResource(R.string.today_task_completed)
    val undoLabel = stringResource(R.string.action_undo)
    var quickAdd by rememberSaveable { mutableStateOf(false) }

    TodayContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onCompleteTask = { taskId ->
            viewModel.setTaskCompleted(taskId, true)
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(completedMessage, undoLabel, duration = SnackbarDuration.Short)
                if (result == SnackbarResult.ActionPerformed) viewModel.setTaskCompleted(taskId, false)
            }
        },
        onOpenGoal = onOpenGoal,
        onQuickAdd = { quickAdd = true },
    )

    if (quickAdd) {
        QuickAddSheet(
            goals = state.goals,
            today = state.date,
            onDismiss = { quickAdd = false },
            onOpenGoals = {
                quickAdd = false
                onOpenGoals()
            },
            onSave = { title, goalId, due ->
                viewModel.addTask(title, goalId, due)
                quickAdd = false
            },
        )
    }
}

@Composable
fun TodayContent(
    state: TodayUiState,
    onCompleteTask: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    onQuickAdd: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = navBar + FloatingBarClearance + 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "header") { TodayHeader(state) }
                item(key = "hero") { HeroCard(state, Modifier.padding(top = 12.dp, bottom = 8.dp)) }

                if (state.isEmpty) {
                    item(key = "empty") {
                        val allDone = state.doneToday > 0
                        EmptyState(
                            title = stringResource(if (allDone) R.string.today_all_clear_title else R.string.today_empty_title),
                            message = stringResource(if (allDone) R.string.today_all_clear_message else R.string.today_empty_message),
                        )
                    }
                }
                taskSection(R.string.today_section_overdue, state.overdue, state.date, onCompleteTask, onOpenGoal, overdue = true)
                taskSection(R.string.today_section_due, state.dueToday, state.date, onCompleteTask, onOpenGoal, overdue = false)

                state.stats?.let { stats ->
                    item(key = "activity-header") {
                        SectionHeader(stringResource(R.string.today_section_activity), Modifier.padding(top = 24.dp, bottom = 4.dp))
                    }
                    item(key = "activity") {
                        AtomicCard {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    stringResource(R.string.heatmap_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                val total = stats.days.sumOf { it.count }
                                Text(
                                    pluralStringResource(R.plurals.tasks_done, total, total),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            ActivityHeatmap(stats.days)
                        }
                    }
                }
            }
        }

        val bottom = navBar + FloatingBarClearance
        FloatingActionButton(
            onClick = onQuickAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = bottom),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.task_add))
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = bottom + 72.dp))
    }
}

@Composable
private fun TodayHeader(state: TodayUiState) {
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            in 17..21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
    }
    val locale = LocalConfiguration.current.locales[0]
    val dateLabel = remember(state.date, locale) { state.date.format(DateTimeFormatter.ofPattern("EEEE · d MMMM", locale)) }
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                dateLabel.uppercase(locale),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(greeting), style = MaterialTheme.typography.headlineLarge)
            Text(
                if (state.remaining == 0) {
                    stringResource(R.string.today_nothing_left)
                } else {
                    pluralStringResource(R.plurals.today_tasks_left, state.remaining, state.remaining)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painterResource(R.drawable.ic_atom),
            contentDescription = null,
            tint = if (AtomicTheme.colors.isDark) AtomicTheme.colors.accent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp).size(28.dp),
        )
    }
}

@Composable
private fun HeroCard(state: TodayUiState, modifier: Modifier = Modifier) {
    val stats = state.stats
    val streak = stats?.currentStreak ?: 0
    AtomicCard(modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = state.dayProgress, size = 132.dp, strokeWidth = 14.dp) {
                if (state.remaining == 0 && state.doneToday > 0) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.today_all_clear_title),
                        tint = AtomicTheme.colors.accent,
                        modifier = Modifier.size(48.dp),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.doneToday}/${state.doneToday + state.remaining}", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            stringResource(R.string.today_ring_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.width(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    streak.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = if (streak > 0) AtomicTheme.colors.ember else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(stringResource(R.string.stats_current_streak), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.stats_best_and_week, stats?.longestStreak ?: 0, stats?.completedThisWeek ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (stats != null) {
            Spacer(Modifier.height(24.dp))
            WeekStrip(stats.lastSevenDays)
        }
    }
}

private fun LazyListScope.taskSection(
    titleRes: Int,
    tasks: List<TaskWithGoal>,
    today: LocalDate,
    onCompleteTask: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    overdue: Boolean,
) {
    if (tasks.isEmpty()) return
    item(key = "header-$titleRes") {
        SectionHeader(
            stringResource(titleRes),
            Modifier.padding(top = 16.dp, bottom = 4.dp),
            count = tasks.size,
            color = if (overdue) AtomicTheme.colors.ember else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    items(tasks, key = { it.task.id }) { item ->
        TaskRow(
            task = item.task,
            today = today,
            goalTitle = item.goalTitle,
            goalColor = goalColor(item.goalColorIndex),
            onCheckedChange = { checked -> if (checked) onCompleteTask(item.task.id) },
            onClick = { onOpenGoal(item.task.goalId) },
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
private fun QuickAddSheet(
    goals: List<Goal>,
    today: LocalDate,
    onDismiss: () -> Unit,
    onOpenGoals: () -> Unit,
    onSave: (title: String, goalId: Long, due: LocalDate?) -> Unit,
) {
    if (goals.isEmpty()) {
        EditorSheet(
            title = stringResource(R.string.quick_add_no_goals_title),
            confirmLabel = stringResource(R.string.quick_add_open_goals),
            confirmEnabled = true,
            onConfirm = onOpenGoals,
            onDismiss = onDismiss,
        ) {
            Text(stringResource(R.string.quick_add_no_goals_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var title by rememberSaveable { mutableStateOf("") }
    var goalId by rememberSaveable { mutableStateOf(goals.first().id) }
    var due by rememberSaveable { mutableStateOf<LocalDate?>(today) }

    EditorSheet(
        title = stringResource(R.string.task_add),
        confirmLabel = stringResource(R.string.action_save),
        confirmEnabled = title.isNotBlank(),
        onConfirm = { onSave(title, goalId, due) },
        onDismiss = onDismiss,
    ) {
        FieldText(title, { title = it }, placeholder = stringResource(R.string.task_title_placeholder))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { goal ->
                FilterChip(
                    selected = goal.id == goalId,
                    onClick = { goalId = goal.id },
                    label = { Text(goal.title) },
                    leadingIcon = { Box(Modifier.size(10.dp).background(goalColor(goal.colorIndex), CircleShape)) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
        DatePickerButton(date = due, emptyLabel = stringResource(R.string.task_no_due_date), onDateChange = { due = it })
    }
}

@Preview
@Composable
private fun TodayContentPreview() {
    val today = LocalDate.of(2026, 3, 14)
    val zone = ZoneId.systemDefault()
    AtomicTheme {
        TodayContent(
            state = TodayUiState(
                date = today,
                isLoading = false,
                overdue = listOf(
                    TaskWithGoal(Task(1, 1, "Book a physio session", dueAt = today.minusDays(2).startOfDay(zone)), "Run a 10k", 0),
                ),
                dueToday = listOf(
                    TaskWithGoal(Task(2, 1, "Tempo run, 6k", dueAt = today.startOfDay(zone)), "Run a 10k", 0),
                    TaskWithGoal(Task(3, 2, "Read chapter 4", dueAt = today.startOfDay(zone)), "Read 12 books", 1),
                ),
                stats = StreakCalculator.calculate(
                    (0L..40L).filter { it % 3 != 1L }.map { today.minusDays(it) } + List(4) { today },
                    today,
                ),
            ),
            onCompleteTask = {},
            onOpenGoal = {},
        )
    }
}
