package com.myth.atomic.presentation.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myth.atomic.R
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.presentation.components.AtomicCard
import com.myth.atomic.presentation.components.EmptyState
import com.myth.atomic.presentation.components.FloatingBarClearance
import com.myth.atomic.presentation.components.SectionHeader
import com.myth.atomic.presentation.components.Tag
import com.myth.atomic.presentation.components.toLocalDate
import com.myth.atomic.presentation.theme.AtomicTheme
import com.myth.atomic.presentation.theme.goalColor
import java.time.Instant
import java.time.YearMonth
import java.time.format.TextStyle

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

@Composable
fun JournalContent(
    state: JournalUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onNewEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val locale = LocalConfiguration.current.locales[0]
    val byMonth = state.entries.groupBy { YearMonth.from(it.entry.createdAt.toLocalDate()) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = navBar + FloatingBarClearance + 72.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.nav_journal), style = MaterialTheme.typography.headlineLarge)
                    SearchField(query, onQueryChange)
                }
            }
            when {
                state.isLoading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                state.entries.isEmpty() -> item(key = "empty") {
                    EmptyState(
                        title = stringResource(if (state.isSearching) R.string.journal_no_results_title else R.string.journal_empty_title),
                        message = stringResource(if (state.isSearching) R.string.journal_no_results_message else R.string.journal_empty_message),
                        icon = if (state.isSearching) Icons.Default.Search else null,
                    )
                }
                else -> byMonth.forEach { (month, entries) ->
                    item(key = "month-$month") {
                        SectionHeader(
                            "${month.month.getDisplayName(TextStyle.FULL, locale)} ${month.year}",
                            Modifier.padding(top = 12.dp, bottom = 2.dp),
                        )
                    }
                    items(entries, key = { it.entry.id }) { item ->
                        JournalCard(item, onClick = { onOpenEntry(item.entry.id) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewEntry,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = navBar + FloatingBarClearance),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.journal_new))
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val container = MaterialTheme.colorScheme.surfaceContainer
    TextField(
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
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun JournalCard(item: JournalItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val entry = item.entry
    val date = entry.createdAt.toLocalDate()
    val locale = LocalConfiguration.current.locales[0]
    AtomicCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(18.dp)) {
        Row {
            Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.headlineSmall)
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    entry.title.ifBlank { stringResource(R.string.journal_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (entry.body.isNotBlank()) {
                    Text(
                        entry.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                item.goalTitle?.let { Tag(it, dotColor = goalColor(item.goalColorIndex)) }
            }
        }
    }
}

@Preview
@Composable
private fun JournalContentPreview() {
    val now = Instant.parse("2026-03-14T09:00:00Z")
    AtomicTheme {
        JournalContent(
            state = JournalUiState(
                isLoading = false,
                entries = listOf(
                    JournalItem(JournalEntry(1, "First run", "Harder than expected, but I finished.", now, goalId = 1), "Run a 10k", 0),
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
