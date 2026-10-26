package com.myth.atomic.presentation.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myth.atomic.R
import com.myth.atomic.presentation.components.DatePickerButton
import com.myth.atomic.presentation.components.EditorSheet
import com.myth.atomic.presentation.components.FieldText
import com.myth.atomic.presentation.components.GoalColorPicker
import java.time.LocalDate

/** Creates a goal, or edits one when [isNew] is false. */
@Composable
fun GoalEditorSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, deadline: LocalDate?, colorIndex: Int) -> Unit,
    isNew: Boolean = true,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDeadline: LocalDate? = null,
    initialColorIndex: Int = 0,
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var deadline by rememberSaveable { mutableStateOf(initialDeadline) }
    var colorIndex by rememberSaveable { mutableIntStateOf(initialColorIndex) }

    EditorSheet(
        title = stringResource(if (isNew) R.string.goal_new else R.string.goal_edit),
        confirmLabel = stringResource(if (isNew) R.string.goal_create else R.string.action_save),
        confirmEnabled = title.isNotBlank(),
        onConfirm = { onSave(title, description, deadline, colorIndex) },
        onDismiss = onDismiss,
    ) {
        FieldText(
            title,
            { title = it },
            placeholder = stringResource(R.string.goal_title_placeholder),
            textStyle = MaterialTheme.typography.titleLarge,
        )
        FieldText(
            description,
            { description = it },
            placeholder = stringResource(R.string.goal_description_label),
            singleLine = false,
            minLines = 2,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.goal_color_label), style = MaterialTheme.typography.labelLarge)
            GoalColorPicker(selected = colorIndex, onSelect = { colorIndex = it })
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.goal_deadline_label), style = MaterialTheme.typography.labelLarge)
            DatePickerButton(date = deadline, emptyLabel = stringResource(R.string.goal_no_deadline), onDateChange = { deadline = it })
        }
    }
}
