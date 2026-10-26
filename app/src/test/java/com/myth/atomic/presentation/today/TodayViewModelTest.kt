package com.myth.atomic.presentation.today

import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.Task
import com.myth.atomic.testing.FakeAtomicRepository
import com.myth.atomic.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-03-14T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val repository = FakeAtomicRepository(clock)

    @Test
    fun splitsOverdueAndDueTodayAndSkipsLaterTasks() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        repository.addTask(task(goalId, "Overdue", now.minus(Duration.ofDays(2))))
        repository.addTask(task(goalId, "This evening", now.plus(Duration.ofHours(10))))
        repository.addTask(task(goalId, "Tomorrow", now.plus(Duration.ofDays(1))))
        repository.addTask(task(goalId, "Someday", null))

        val viewModel = TodayViewModel(repository, repository, clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Overdue"), state.overdue.map { it.task.title })
        assertEquals(listOf("This evening"), state.dueToday.map { it.task.title })
        assertEquals("Run a 10k", state.dueToday.single().goalTitle)
    }

    @Test
    fun completingTaskRemovesItAndUndoRestoresIt() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val taskId = repository.addTask(task(goalId, "Easy 5k", now))

        val viewModel = TodayViewModel(repository, repository, clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.setTaskCompleted(taskId, true)
        assertTrue(viewModel.uiState.value.isEmpty)

        viewModel.setTaskCompleted(taskId, false)
        assertEquals(1, viewModel.uiState.value.dueToday.size)
    }

    private fun task(goalId: Long, title: String, dueAt: Instant?) =
        Task(goalId = goalId, title = title, dueAt = dueAt, createdAt = now)
}
