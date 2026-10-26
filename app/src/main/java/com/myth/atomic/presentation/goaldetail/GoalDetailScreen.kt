package com.myth.atomic.presentation.goaldetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.GoalDetails
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.domain.model.Task
import com.myth.atomic.presentation.components.DatePickerButton
import com.myth.atomic.presentation.components.TaskRow
import com.myth.atomic.presentation.components.formatMedium
import com.myth.atomic.presentation.components.startOfDay
import com.myth.atomic.presentation.components.toLocalDate
import com.myth.atomic.presentation.goals.GoalEditorDialog
import com.myth.atomic.presentation.goals.taskProgressLabel
import com.myth.atomic.presentation.theme.AtomicTheme
import java.time.LocalDate
import java.time.ZoneId

private enum class GoalDialog { None, EditGoal, AddTask, ConfirmDelete }

@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    onWriteEntry: (goalId: Long) -> Unit,
    onOpenEntry: (entryId: Long) -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableStateOf(GoalDialog.None) }

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
        onWriteEntry = onWriteEntry,
        onOpenEntry = onOpenEntry,
    )

    val loaded = state as? GoalDetailUiState.Loaded ?: return
    val goal = loaded.details.goal
    when (dialog) {
        GoalDialog.None -> Unit
        GoalDialog.EditGoal -> GoalEditorDialog(
            initialTitle = goal.title,
            initialDescription = goal.description,
            initialDeadline = goal.deadline?.toLocalDate(),
            onDismiss = { dialog = GoalDialog.None },
            onSave = { title, description, deadline ->
                viewModel.updateGoal(title, description, deadline)
                dialog = GoalDialog.None
            },
        )
        GoalDialog.AddTask -> AddTaskDialog(
            onDismiss = { dialog = GoalDialog.None },
            onSave = { title, due ->
                viewModel.addTask(title, due)
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
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { dialog = GoalDialog.None }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onWriteEntry: (goalId: Long) -> Unit,
    onOpenEntry: (entryId: Long) -> Unit,
) {
    val loaded = state as? GoalDetailUiState.Loaded
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(loaded?.details?.goal?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (loaded != null) {
                        IconButton(onClick = onEditGoal) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.goal_edit))
                        }
                        GoalOverflowMenu(loaded.details.goal.isCompleted, onSetGoalCompleted, onDeleteGoal)
                    }
                },
            )
        },
        floatingActionButton = {
            if (loaded != null) {
                ExtendedFloatingActionButton(
                    onClick = onAddTask,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.task_add)) },
                )
            }
        },
    ) { padding ->
        if (loaded == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val details = loaded.details
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp,
            ),
        ) {
            item(key = "summary") { GoalSummary(loaded) }

            item(key = "tasks-header") { SectionHeader(stringResource(R.string.goal_section_tasks)) }
            if (details.tasks.isEmpty()) {
                item(key = "tasks-empty") { HintText(stringResource(R.string.goal_tasks_empty)) }
            }
            items(details.tasks, key = { "task-${it.id}" }) { task ->
                TaskRow(
                    task = task,
                    today = loaded.today,
                    onCheckedChange = { onSetTaskCompleted(task.id, it) },
                    onDelete = { onDeleteTask(task.id) },
                    modifier = Modifier.animateItem(),
                )
            }

            item(key = "journal-header") {
                SectionHeader(stringResource(R.string.goal_section_journal)) {
                    TextButton(onClick = { onWriteEntry(details.goal.id) }) { Text(stringResource(R.string.journal_write)) }
                }
            }
            if (details.journalEntries.isEmpty()) {
                item(key = "journal-empty") { HintText(stringResource(R.string.goal_journal_empty)) }
            }
            items(details.journalEntries, key = { "entry-${it.id}" }) { entry ->
                JournalEntryRow(entry, onClick = { onOpenEntry(entry.id) })
            }
        }
    }
}

@Composable
private fun GoalOverflowMenu(isCompleted: Boolean, onSetGoalCompleted: (Boolean) -> Unit, onDeleteGoal: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
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
                text = { Text(stringResource(R.string.goal_delete_title)) },
                onClick = {
                    expanded = false
                    onDeleteGoal()
                },
            )
        }
    }
}

@Composable
private fun GoalSummary(state: GoalDetailUiState.Loaded) {
    val goal = state.details.goal
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (goal.description.isNotBlank()) {
            Text(goal.description, style = MaterialTheme.typography.bodyLarge)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            goal.deadline?.let {
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.goal_deadline, it.toLocalDate().formatMedium())) })
            }
            if (goal.isCompleted) {
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.goal_completed)) })
            }
        }
        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
        Text(
            taskProgressLabel(state.completedTasks, state.details.tasks.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider()
}

@Composable
private fun SectionHeader(title: String, action: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        action()
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun JournalEntryRow(entry: JournalEntry, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(entry.title.ifBlank { entry.body }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(entry.createdAt.toLocalDate().formatMedium()) },
    )
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, LocalDate?) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var due by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.task_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_title_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                DatePickerButton(date = due, emptyLabel = stringResource(R.string.task_no_due_date), onDateChange = { due = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, due) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )
}

@Preview
@Composable
private fun GoalDetailPreview() {
    val today = LocalDate.of(2026, 3, 14)
    val zone = ZoneId.systemDefault()
    val now = today.startOfDay(zone)
    AtomicTheme(dynamicColor = false) {
        GoalDetailContent(
            state = GoalDetailUiState.Loaded(
                details = GoalDetails(
                    goal = Goal(1, "Run a 10k", "Spring race in May.", deadline = today.plusDays(60).startOfDay(zone), createdAt = now),
                    tasks = listOf(
                        Task(1, 1, "Buy running shoes", createdAt = now, completedAt = now),
                        Task(2, 1, "Easy 5k run", dueAt = now, createdAt = now),
                        Task(3, 1, "Sign up for the race", dueAt = today.plusDays(3).startOfDay(zone), createdAt = now),
                    ),
                    reminders = emptyList(),
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
            onWriteEntry = {},
            onOpenEntry = {},
        )
    }
}
