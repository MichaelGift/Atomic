package com.myth.atomic.presentation.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.myth.atomic.presentation.components.EmptyState
import com.myth.atomic.presentation.components.formatMedium
import com.myth.atomic.presentation.components.toLocalDate
import com.myth.atomic.presentation.theme.AtomicTheme
import java.time.Instant

@Composable
fun GoalsScreen(
    onOpenGoal: (Long) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditor by rememberSaveable { mutableStateOf(false) }

    GoalsContent(state = state, onOpenGoal = onOpenGoal, onAddGoal = { showEditor = true })

    if (showEditor) {
        GoalEditorDialog(
            onDismiss = { showEditor = false },
            onSave = { title, description, deadline ->
                viewModel.addGoal(title, description, deadline)
                showEditor = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsContent(
    state: GoalsUiState,
    onOpenGoal: (Long) -> Unit,
    onAddGoal: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.nav_goals)) }, scrollBehavior = scrollBehavior)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddGoal,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.goal_new)) },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.isEmpty -> EmptyState(
                icon = Icons.Default.Star,
                title = stringResource(R.string.goals_empty_title),
                message = stringResource(R.string.goals_empty_message),
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding(),
                    // Leave room for the FAB.
                    bottom = padding.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                goalSection(null, state.active, onOpenGoal)
                goalSection(R.string.goals_section_completed, state.completed, onOpenGoal)
            }
        }
    }
}

private fun LazyListScope.goalSection(titleRes: Int?, goals: List<GoalWithProgress>, onOpenGoal: (Long) -> Unit) {
    if (goals.isEmpty()) return
    if (titleRes != null) {
        item(key = "header-$titleRes") {
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
    items(goals, key = { it.goal.id }) { goal ->
        GoalCard(goal, onClick = { onOpenGoal(goal.goal.id) }, modifier = Modifier.animateItem())
    }
}

@Composable
private fun GoalCard(item: GoalWithProgress, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.goal.title, style = MaterialTheme.typography.titleMedium)
            if (item.goal.description.isNotBlank()) {
                Text(
                    item.goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LinearProgressIndicator(progress = { item.progress }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    taskProgressLabel(item.completedTasks, item.totalTasks),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                item.goal.deadline?.let {
                    Text(
                        stringResource(R.string.goal_deadline, it.toLocalDate().formatMedium()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun taskProgressLabel(completed: Int, total: Int): String =
    if (total == 0) stringResource(R.string.goal_no_tasks)
    else pluralStringResource(R.plurals.goal_tasks_progress, total, completed, total)

@Preview
@Composable
private fun GoalsContentPreview() {
    val now = Instant.parse("2026-03-14T09:00:00Z")
    AtomicTheme(dynamicColor = false) {
        GoalsContent(
            state = GoalsUiState(
                isLoading = false,
                active = listOf(
                    GoalWithProgress(Goal(1, "Run a 10k", "Spring race in May", deadline = now.plusSeconds(60L * 86_400), createdAt = now), 6, 4),
                    GoalWithProgress(Goal(2, "Read 12 books", createdAt = now), 12, 3),
                ),
                completed = listOf(
                    GoalWithProgress(Goal(3, "Learn Compose", createdAt = now, completedAt = now), 5, 5),
                ),
            ),
            onOpenGoal = {},
            onAddGoal = {},
        )
    }
}
