package com.myth.atomic.presentation.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.Task
import com.myth.atomic.presentation.components.EmptyState
import com.myth.atomic.presentation.components.TaskRow
import com.myth.atomic.presentation.components.formatFull
import com.myth.atomic.presentation.components.startOfDay
import com.myth.atomic.presentation.theme.AtomicTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TodayScreen(
    onOpenGoal: (Long) -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val completedMessage = stringResource(R.string.today_task_completed)
    val undoLabel = stringResource(R.string.action_undo)

    TodayContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onCompleteTask = { taskId ->
            viewModel.setTaskCompleted(taskId, true)
            scope.launch {
                val result = snackbarHostState.showSnackbar(completedMessage, undoLabel, duration = SnackbarDuration.Short)
                if (result == SnackbarResult.ActionPerformed) viewModel.setTaskCompleted(taskId, false)
            }
        },
        onOpenGoal = onOpenGoal,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    state: TodayUiState,
    onCompleteTask: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.nav_today)) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.isEmpty -> EmptyState(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.today_empty_title),
                message = stringResource(R.string.today_empty_message),
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                item {
                    Text(
                        state.date.formatFull(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                taskSection(R.string.today_section_overdue, state.overdue, state.date, onCompleteTask, onOpenGoal)
                taskSection(R.string.today_section_due, state.dueToday, state.date, onCompleteTask, onOpenGoal)
            }
        }
    }
}

private fun LazyListScope.taskSection(
    titleRes: Int,
    tasks: List<TaskWithGoal>,
    today: LocalDate,
    onCompleteTask: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
) {
    if (tasks.isEmpty()) return
    item(key = "header-$titleRes") {
        Text(
            stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        )
    }
    items(tasks, key = { it.task.id }) { item ->
        TaskRow(
            task = item.task,
            today = today,
            goalTitle = item.goalTitle,
            onCheckedChange = { checked -> if (checked) onCompleteTask(item.task.id) },
            modifier = Modifier.animateItem().clickable { onOpenGoal(item.task.goalId) },
        )
    }
}

@Preview
@Composable
private fun TodayContentPreview() {
    val today = LocalDate.of(2026, 3, 14)
    val zone = ZoneId.systemDefault()
    AtomicTheme(dynamicColor = false) {
        TodayContent(
            state = TodayUiState(
                date = today,
                isLoading = false,
                overdue = listOf(
                    TaskWithGoal(Task(1, 1, "Book a physio session", dueAt = today.minusDays(2).startOfDay(zone)), "Run a 10k"),
                ),
                dueToday = listOf(
                    TaskWithGoal(Task(2, 1, "Easy 5k run", dueAt = today.startOfDay(zone)), "Run a 10k"),
                    TaskWithGoal(Task(3, 2, "Read chapter 4", dueAt = today.startOfDay(zone)), "Read 12 books"),
                ),
            ),
            onCompleteTask = {},
            onOpenGoal = {},
        )
    }
}

@Preview
@Composable
private fun TodayEmptyPreview() {
    AtomicTheme(dynamicColor = false) {
        TodayContent(TodayUiState(date = LocalDate.of(2026, 3, 14), isLoading = false), onCompleteTask = {}, onOpenGoal = {})
    }
}
