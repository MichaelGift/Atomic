package com.myth.atomic.presentation.journal

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.presentation.components.EmptyState
import com.myth.atomic.presentation.components.formatMedium
import com.myth.atomic.presentation.components.toLocalDate
import com.myth.atomic.presentation.theme.AtomicTheme
import java.time.Instant

@Composable
fun JournalScreen(
    onNewEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    JournalContent(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onNewEntry = onNewEntry,
        onOpenEntry = onOpenEntry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalContent(
    state: JournalUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onNewEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.nav_journal)) }, scrollBehavior = scrollBehavior)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEntry,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.journal_new)) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.journal_search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_search))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.entries.isEmpty() -> EmptyState(
                    icon = if (state.isSearching) Icons.Default.Search else Icons.Default.Create,
                    title = stringResource(if (state.isSearching) R.string.journal_no_results_title else R.string.journal_empty_title),
                    message = stringResource(if (state.isSearching) R.string.journal_no_results_message else R.string.journal_empty_message),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = padding.calculateBottomPadding() + 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.entries, key = { it.entry.id }) { item ->
                        JournalCard(item, onClick = { onOpenEntry(item.entry.id) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalCard(item: JournalItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val entry = item.entry
    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    entry.title.ifBlank { stringResource(R.string.journal_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    entry.createdAt.toLocalDate().formatMedium(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (entry.body.isNotBlank()) {
                Text(
                    entry.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            item.goalTitle?.let { SuggestionChip(onClick = onClick, label = { Text(it) }) }
        }
    }
}

@Preview
@Composable
private fun JournalContentPreview() {
    val now = Instant.parse("2026-03-14T09:00:00Z")
    AtomicTheme(dynamicColor = false) {
        JournalContent(
            state = JournalUiState(
                isLoading = false,
                entries = listOf(
                    JournalItem(JournalEntry(1, "First run", "Harder than expected, but I finished.", now, goalId = 1), "Run a 10k"),
                    JournalItem(JournalEntry(2, "", "Slept badly; skipped reading.", now.minusSeconds(86_400)), null),
                ),
            ),
            query = "",
            onQueryChange = {},
            onNewEntry = {},
            onOpenEntry = {},
        )
    }
}
