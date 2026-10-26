package com.myth.atomic.presentation.journal.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.myth.atomic.R
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.presentation.theme.AtomicTheme
import com.myth.atomic.presentation.theme.goalColor
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun JournalEditorScreen(
    onDone: () -> Unit,
    viewModel: JournalEditorViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    LaunchedEffect(state.isDone) {
        if (state.isDone) onDone()
    }
    JournalEditorContent(
        state = state,
        onClose = onDone,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onGoalChange = viewModel::onGoalChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
    )
}

@Composable
fun JournalEditorContent(
    state: JournalEditorUiState,
    onClose: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onGoalChange: (Long?) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onClose,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
            }
            Text(
                stringResource(if (state.isNew) R.string.journal_new else R.string.journal_edit),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            if (!state.isNew) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.journal_delete))
                }
                Spacer(Modifier.width(4.dp))
            }
            Button(
                onClick = onSave,
                enabled = state.canSave,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) { Text(stringResource(R.string.action_save)) }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            GoalChips(
                goals = state.goals,
                selectedGoalId = state.goalId,
                onGoalChange = onGoalChange,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            PlainField(
                value = state.title,
                onValueChange = onTitleChange,
                placeholder = stringResource(R.string.journal_title_placeholder),
                textStyle = MaterialTheme.typography.headlineMedium,
                singleLine = true,
            )
            PlainField(
                value = state.body,
                onValueChange = onBodyChange,
                placeholder = stringResource(R.string.journal_body_placeholder),
                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f),
                singleLine = false,
            )
        }
    }
}

@Composable
private fun PlainField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    singleLine: Boolean,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = textStyle) },
        textStyle = textStyle,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    )
}

/** Links the entry to one goal, or none; scrolls sideways when there are many. */
@Composable
private fun GoalChips(goals: List<Goal>, selectedGoalId: Long?, onGoalChange: (Long?) -> Unit, modifier: Modifier = Modifier) {
    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedGoalId == null,
            onClick = { onGoalChange(null) },
            label = { Text(stringResource(R.string.journal_no_goal)) },
            shape = CircleShape,
            colors = colors,
        )
        goals.forEach { goal ->
            FilterChip(
                selected = goal.id == selectedGoalId,
                onClick = { onGoalChange(goal.id) },
                label = { Text(goal.title) },
                leadingIcon = { Box(Modifier.size(10.dp).background(goalColor(goal.colorIndex), CircleShape)) },
                shape = CircleShape,
                colors = colors,
            )
        }
    }
}

@Preview
@Composable
private fun JournalEditorPreview() {
    AtomicTheme {
        JournalEditorContent(
            state = JournalEditorUiState(
                isNew = false,
                isLoading = false,
                title = "First run",
                body = "Harder than expected, but I finished without stopping.",
                goalId = 1,
                goals = listOf(Goal(1, "Run a 10k"), Goal(2, "Read 12 books", colorIndex = 1)),
            ),
            onClose = {},
            onTitleChange = {},
            onBodyChange = {},
            onGoalChange = {},
            onSave = {},
            onDelete = {},
        )
    }
}
