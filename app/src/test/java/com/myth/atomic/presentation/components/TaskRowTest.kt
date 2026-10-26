package com.myth.atomic.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.myth.atomic.domain.model.Task
import com.myth.atomic.presentation.theme.AtomicTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class TaskRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 3, 14)
    private val lastWeek = today.minusDays(7)
    private val dueLastWeek = lastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant()

    @Test
    fun openTaskPastItsDueDateIsOverdue() {
        render(Task(id = 1, goalId = 1, title = "Book physio", dueAt = dueLastWeek))

        composeRule.onNodeWithText("Overdue · ${lastWeek.formatMedium()}").assertIsDisplayed()
    }

    @Test
    fun completedTaskIsNeverOverdue() {
        render(Task(id = 1, goalId = 1, title = "Book physio", dueAt = dueLastWeek, completedAt = dueLastWeek))

        composeRule.onNodeWithText("Due ${lastWeek.formatMedium()}").assertIsDisplayed()
    }

    private fun render(task: Task) {
        composeRule.setContent {
            AtomicTheme {
                TaskRow(task = task, today = today, onCheckedChange = {})
            }
        }
    }
}
