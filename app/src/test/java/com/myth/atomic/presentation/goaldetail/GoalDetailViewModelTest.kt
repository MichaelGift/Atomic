package com.myth.atomic.presentation.goaldetail

import androidx.lifecycle.SavedStateHandle
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.RepeatInterval
import com.myth.atomic.domain.reminders.AddReminderUseCase
import com.myth.atomic.domain.reminders.DeleteReminderUseCase
import com.myth.atomic.testing.FakeAtomicRepository
import com.myth.atomic.testing.FakeReminderScheduler
import com.myth.atomic.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

// Robolectric because SavedStateHandle.toRoute() decodes through Android's Bundle.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GoalDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-03-14T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val repository = FakeAtomicRepository(clock)
    private val scheduler = FakeReminderScheduler()

    @Test
    fun addingAndCompletingTasksUpdatesProgress() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val viewModel = viewModel(goalId)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.addTask(" Buy shoes ", dueDate = LocalDate.of(2026, 3, 20))
        viewModel.addTask("First 5k", dueDate = null)
        val firstTask = repository.tasks.value.first()
        viewModel.setTaskCompleted(firstTask.id, true)

        val state = viewModel.uiState.value as GoalDetailUiState.Loaded
        assertEquals("Buy shoes", firstTask.title)
        assertEquals(Instant.parse("2026-03-20T00:00:00Z"), firstTask.dueAt)
        assertEquals(0.5f, state.progress)
    }

    @Test
    fun deletingGoalMovesToNotFound() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val viewModel = viewModel(goalId)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.deleteGoal()

        assertEquals(GoalDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun updateGoalKeepsCompletionAndCreationTime() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now, completedAt = now))
        val viewModel = viewModel(goalId)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.updateGoal("Run a half marathon", "", deadline = null, colorIndex = 2)

        val goal = repository.goals.value.single()
        assertEquals("Run a half marathon", goal.title)
        assertEquals(2, goal.colorIndex)
        assertEquals(now, goal.createdAt)
        assertEquals(now, goal.completedAt)
    }

    private fun viewModel(goalId: Long) =
        GoalDetailViewModel(
            SavedStateHandle(mapOf("goalId" to goalId)),
            repository,
            repository,
            AddReminderUseCase(repository, scheduler),
            DeleteReminderUseCase(repository, scheduler),
            clock,
        )

    @Test
    fun addingReminderSchedulesAlarmAndDeletingGoalCancelsIt() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val viewModel = viewModel(goalId)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.addReminder("Go for a run", LocalDate.of(2026, 3, 15), LocalTime.of(7, 30), RepeatInterval.DAILY)

        val reminder = repository.reminders.value.single()
        assertEquals(Instant.parse("2026-03-15T07:30:00Z"), reminder.triggerAt)
        assertEquals(mapOf(reminder.id to reminder.triggerAt), scheduler.scheduled)

        viewModel.deleteGoal()
        assertEquals(emptyMap<Long, Instant>(), scheduler.scheduled)
    }
}
