package com.myth.atomic.presentation.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performClick
import com.myth.atomic.domain.model.Task
import com.myth.atomic.presentation.theme.AtomicTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class TodayContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 3, 14)

    @Test
    fun showsEmptyStateWhenNothingIsDue() {
        composeRule.setContent {
            AtomicTheme(dynamicColor = false) {
                TodayContent(TodayUiState(date = today, isLoading = false), onCompleteTask = {}, onOpenGoal = {})
            }
        }

        composeRule.onNodeWithText("Nothing due today").assertIsDisplayed()
    }

    @Test
    fun checkingTaskReportsCompletion() {
        var completed: Long? = null
        val dueAt = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        composeRule.setContent {
            AtomicTheme(dynamicColor = false) {
                TodayContent(
                    TodayUiState(
                        date = today,
                        isLoading = false,
                        dueToday = listOf(TaskWithGoal(Task(id = 7, goalId = 1, title = "Easy 5k", dueAt = dueAt), "Run a 10k")),
                    ),
                    onCompleteTask = { completed = it },
                    onOpenGoal = {},
                )
            }
        }

        composeRule.onNodeWithText("Easy 5k").assertIsDisplayed()
        composeRule.onNodeWithText("Run a 10k · Due today").assertIsDisplayed()
        composeRule.onAllNodes(isToggleable())[0].performClick()

        assertEquals(7L, completed)
    }
}
