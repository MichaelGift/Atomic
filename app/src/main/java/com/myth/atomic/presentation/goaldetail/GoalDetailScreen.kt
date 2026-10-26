package com.myth.atomic.presentation.goaldetail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.model.Reminder
import com.myth.atomic.domain.model.RepeatInterval
import com.myth.atomic.domain.model.Task
import com.myth.atomic.presentation.components.AtomicCard
import com.myth.atomic.presentation.components.DatePickerButton
import com.myth.atomic.presentation.components.EditorSheet
import com.myth.atomic.presentation.components.FieldText
import com.myth.atomic.presentation.components.ProgressRing
import com.myth.atomic.presentation.components.SectionHeader
import com.myth.atomic.presentation.components.Tag
import com.myth.atomic.presentation.components.TaskRow
import com.myth.atomic.presentation.components.startOfDay
import com.myth.atomic.presentation.components.toLocalDate
import com.myth.atomic.presentation.goals.DeadlineTag
import com.myth.atomic.presentation.goals.GoalEditorSheet
import com.myth.atomic.presentation.goals.taskProgressLabel
import com.myth.atomic.presentation.theme.AtomicTheme
import com.myth.atomic.presentation.theme.goalColor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import kotlin.math.roundToInt

private enum class GoalDialog { None, EditGoal, AddTask, AddReminder, ConfirmDelete }

@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    onWriteEntry: (goalId: Long) -> Unit,
    onOpenEntry: (entryId: Long) -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableStateOf(GoalDialog.None) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val notificationsOff = stringResource(R.string.reminder_notifications_off)

    // Reminders are useless without notifications, so ask the first time one is added.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        dialog = GoalDialog.AddReminder
        if (!granted) scope.launch { snackbarHostState.showSnackbar(notificationsOff) }
    }
    val onAddReminder = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else dialog = GoalDialog.AddReminder
    }

    LaunchedEffect(state) {
        if (state == GoalDetailUiState.NotFound) onBack()
    }

    GoalDetailContent(
        state = state,
        onBack = onBack,
        onEditGoal = { dialog = GoalDialog.EditGoal },
        onSetGoalCompleted = viewModel::setGoalCompleted,
        onDeleteGoal = { dialog = GoalDialog.ConfirmDelete },
        onAddTask = { dialog = GoalDialog.AddTask },
        onSetTaskCompleted = viewModel::setTaskCompleted,
        onDeleteTask = viewModel::deleteTask,
        onAddReminder = onAddReminder,
        onDeleteReminder = viewModel::deleteReminder,
        onWriteEntry = onWriteEntry,
        onOpenEntry = onOpenEntry,
        snackbarHostState = snackbarHostState,
    )

    val loaded = state as? GoalDetailUiState.Loaded ?: return
    val goal = loaded.details.goal
    when (dialog) {
        GoalDialog.None -> Unit
        GoalDialog.EditGoal -> GoalEditorSheet(
            isNew = false,
            initialTitle = goal.title,
            initialDescription = goal.description,
            initialDeadline = goal.deadline?.toLocalDate(),
            initialColorIndex = goal.colorIndex,
            onDismiss = { dialog = GoalDialog.None },
            onSave = { title, description, deadline, color ->
                viewModel.updateGoal(title, description, deadline, color)
                dialog = GoalDialog.None
            },
        )
        GoalDialog.AddTask -> AddTaskSheet(
            onDismiss = { dialog = GoalDialog.None },
            onSave = { title, due ->
                viewModel.addTask(title, due)
                dialog = GoalDialog.None
            },
        )
        GoalDialog.AddReminder -> AddReminderSheet(
            defaultTitle = goal.title,
            onDismiss = { dialog = GoalDialog.None },
            onSave = { title, date, time, repeat ->
                viewModel.addReminder(title, date, time, repeat)
                dialog = GoalDialog.None
            },
        )
        GoalDialog.ConfirmDelete -> AlertDialog(
            onDismissRequest = { dialog = GoalDialog.None },
            title = { Text(stringResource(R.string.goal_delete_title)) },
            text = { Text(stringResource(R.string.goal_delete_message, goal.title)) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = GoalDialog.None
                    viewModel.deleteGoal()
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { dialog = GoalDialog.None }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}

@Composable
fun GoalDetailContent(
    state: GoalDetailUiState,
    onBack: () -> Unit,
    onEditGoal: () -> Unit,
    onSetGoalCompleted: (Boolean) -> Unit,
    onDeleteGoal: () -> Unit,
    onAddTask: () -> Unit,
    onSetTaskCompleted: (taskId: Long, completed: Boolean) -> Unit,
    onDeleteTask: (taskId: Long) -> Unit,
    onAddReminder: () -> Unit,
    onDeleteReminder: (reminderId: Long) -> Unit,
    onWriteEntry: (goalId: Long) -> Unit,
    onOpenEntry: (entryId: Long) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val loaded = state as? GoalDetailUiState.Loaded
    Box(Modifier.fillMaxSize()) {
        if (loaded == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }
        val details = loaded.details
        val color = goalColor(details.goal.colorIndex)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 104.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "toolbar") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                    Spacer(Modifier.weight(1f))
                    RoundIconButton(onClick = onEditGoal) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.goal_edit))
                    }
                    Spacer(Modifier.width(8.dp))
                    GoalOverflowMenu(details.goal.isCompleted, onSetGoalCompleted, onDeleteGoal)
                }
            }
            item(key = "hero") { GoalHero(loaded, color) }

            item(key = "tasks-header") {
                SectionHeader(
                    stringResource(R.string.goal_section_tasks),
                    Modifier.padding(top = 20.dp, bottom = 4.dp),
                    count = details.tasks.size.takeIf { it > 0 },
                )
            }
            if (details.tasks.isEmpty()) {
                item(key = "tasks-empty") { HintText(stringResource(R.string.goal_tasks_empty)) }
            }
            items(details.tasks, key = { "task-${it.id}" }) { task ->
                TaskRow(
                    task = task,
                    today = loaded.today,
                    goalColor = color,
                    onCheckedChange = { onSetTaskCompleted(task.id, it) },
                    onDelete = { onDeleteTask(task.id) },
                    modifier = Modifier.animateItem(),
                )
            }

            item(key = "reminders-header") {
                SectionHeader(stringResource(R.string.goal_section_reminders), Modifier.padding(top = 20.dp, bottom = 4.dp)) {
                    TextButton(onClick = onAddReminder) { Text(stringResource(R.string.reminder_add_short)) }
                }
            }
            if (details.reminders.isEmpty()) {
                item(key = "reminders-empty") { HintText(stringResource(R.string.goal_reminders_empty)) }
            }
            items(details.reminders, key = { "reminder-${it.id}" }) { reminder ->
                ReminderRow(reminder, color, onDelete = { onDeleteReminder(reminder.id) }, modifier = Modifier.animateItem())
            }

            item(key = "journal-header") {
                SectionHeader(stringResource(R.string.goal_section_journal), Modifier.padding(top = 20.dp, bottom = 4.dp)) {
                    TextButton(onClick = { onWriteEntry(details.goal.id) }) { Text(stringResource(R.string.journal_write)) }
                }
            }
            if (details.journalEntries.isEmpty()) {
                item(key = "journal-empty") { HintText(stringResource(R.string.goal_journal_empty)) }
            }
            items(details.journalEntries, key = { "entry-${it.id}" }) { entry ->
                JournalEntryCard(entry, color, onClick = { onOpenEntry(entry.id) })
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddTask,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.task_add)) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp),
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 88.dp))
    }
}

@Composable
private fun GoalHero(state: GoalDetailUiState.Loaded, color: Color) {
    val goal = state.details.goal
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(goal.title, style = MaterialTheme.typography.headlineLarge)
            if (goal.description.isNotBlank()) {
                Text(goal.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AtomicCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(progress = state.progress, size = 112.dp, strokeWidth = 12.dp, color = color) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(state.progress * 100).roundToInt()}%", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(R.string.goal_ring_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(taskProgressLabel(state.completedTasks, state.details.tasks.size), style = MaterialTheme.typography.titleMedium)
                    when {
                        goal.isCompleted -> Tag(
                            stringResource(R.string.goal_completed),
                            contentColor = AtomicTheme.colors.onAccent,
                            containerColor = AtomicTheme.colors.accent,
                        )
                        goal.deadline != null -> DeadlineTag(goal.deadline.toLocalDate(), state.today)
                        else -> Tag(stringResource(R.string.goal_no_deadline))
                    }
                    goal.deadline?.let {
                        Text(
                            stringResource(R.string.goal_deadline, it.toLocalDate().formatLong()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalDate.formatLong(): String =
    "$dayOfMonth ${month.getDisplayName(TextStyle.FULL, LocalConfiguration.current.locales[0])} $year"

@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        content = content,
    )
}

@Composable
private fun GoalOverflowMenu(isCompleted: Boolean, onSetGoalCompleted: (Boolean) -> Unit, onDeleteGoal: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        RoundIconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(if (isCompleted) R.string.goal_reopen else R.string.goal_mark_complete)) },
                onClick = {
                    expanded = false
                    onSetGoalCompleted(!isCompleted)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.goal_delete_title), color = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDeleteGoal()
                },
            )
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun JournalEntryCard(entry: JournalEntry, color: Color, onClick: () -> Unit) {
    AtomicCard(Modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = 6.dp).size(8.dp).background(color, CircleShape))
            Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    entry.title.ifBlank { stringResource(R.string.journal_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.createdAt.toLocalDate().formatLong(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddTaskSheet(onDismiss: () -> Unit, onSave: (String, LocalDate?) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var due by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    EditorSheet(
        title = stringResource(R.string.task_add),
        confirmLabel = stringResource(R.string.action_save),
        confirmEnabled = title.isNotBlank(),
        onConfirm = { onSave(title, due) },
        onDismiss = onDismiss,
    ) {
        FieldText(title, { title = it }, placeholder = stringResource(R.string.task_title_placeholder))
        DatePickerButton(date = due, emptyLabel = stringResource(R.string.task_no_due_date), onDateChange = { due = it })
    }
}

@Preview
@Composable
private fun GoalDetailPreview() {
    val today = LocalDate.of(2026, 3, 14)
    val zone = ZoneId.systemDefault()
    val now = today.startOfDay(zone)
    AtomicTheme {
        GoalDetailContent(
            state = GoalDetailUiState.Loaded(
                details = GoalDetails(
                    goal = Goal(1, "Run a 10k", "Spring race in May.", deadline = today.plusDays(60).startOfDay(zone), createdAt = now),
                    tasks = listOf(
                        Task(1, 1, "Buy running shoes", createdAt = now, completedAt = now),
                        Task(2, 1, "Easy 5k run", dueAt = now, createdAt = now),
                        Task(3, 1, "Sign up for the race", dueAt = today.plusDays(3).startOfDay(zone), createdAt = now),
                    ),
                    reminders = listOf(Reminder(1, "Lace up", now.plusSeconds(7 * 3600), RepeatInterval.DAILY, goalId = 1)),
                    journalEntries = listOf(JournalEntry(1, "First run", "Harder than expected.", createdAt = now, goalId = 1)),
                ),
                today = today,
            ),
            onBack = {},
            onEditGoal = {},
            onSetGoalCompleted = {},
            onDeleteGoal = {},
            onAddTask = {},
            onSetTaskCompleted = { _, _ -> },
            onDeleteTask = {},
            onAddReminder = {},
            onDeleteReminder = {},
            onWriteEntry = {},
            onOpenEntry = {},
        )
    }
}
