package com.myth.atomic.presentation.journal.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.myth.atomic.R
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.presentation.theme.AtomicTheme

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

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) R.string.journal_new else R.string.journal_edit)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.journal_delete))
                        }
                    }
                    TextButton(onClick = onSave, enabled = state.canSave) { Text(stringResource(R.string.action_save)) }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val plainField = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GoalPicker(
                goals = state.goals,
                selectedGoalId = state.goalId,
                onGoalChange = onGoalChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            TextField(
                value = state.title,
                onValueChange = onTitleChange,
                placeholder = { Text(stringResource(R.string.journal_title_placeholder)) },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true,
                colors = plainField,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = state.body,
                onValueChange = onBodyChange,
                placeholder = { Text(stringResource(R.string.journal_body_placeholder)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = plainField,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalPicker(
    goals: List<Goal>,
    selectedGoalId: Long?,
    onGoalChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val noGoal = stringResource(R.string.journal_no_goal)
    val selectedTitle = goals.firstOrNull { it.id == selectedGoalId }?.title ?: noGoal

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedTitle,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.journal_goal_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(noGoal) },
                onClick = {
                    onGoalChange(null)
                    expanded = false
                },
            )
            goals.forEach { goal ->
                DropdownMenuItem(
                    text = { Text(goal.title) },
                    onClick = {
                        onGoalChange(goal.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun JournalEditorPreview() {
    AtomicTheme(dynamicColor = false) {
        JournalEditorContent(
            state = JournalEditorUiState(
                isNew = false,
                isLoading = false,
                title = "First run",
                body = "Harder than expected, but I finished without stopping.",
                goalId = 1,
                goals = listOf(Goal(1, "Run a 10k")),
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
