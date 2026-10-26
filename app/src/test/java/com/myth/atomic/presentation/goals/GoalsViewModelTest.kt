package com.myth.atomic.presentation.goals

import com.myth.atomic.domain.model.Goal
import com.myth.atomic.testing.FakeAtomicRepository
import com.myth.atomic.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-03-14T09:00:00Z")
    private val zone = ZoneId.of("Asia/Tokyo")
    private val clock = Clock.fixed(now, zone)
    private val repository = FakeAtomicRepository(clock)

    @Test
    fun separatesActiveAndCompletedGoals() = runTest {
        repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        repository.addGoal(Goal(title = "Learn Compose", createdAt = now, completedAt = now))

        val viewModel = GoalsViewModel(repository, clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertEquals(listOf("Run a 10k"), state.active.map { it.goal.title })
        assertEquals(listOf("Learn Compose"), state.completed.map { it.goal.title })
    }

    @Test
    fun addGoalTrimsInputAndStoresDeadlineAtLocalStartOfDay() = runTest {
        val viewModel = GoalsViewModel(repository, clock)

        viewModel.addGoal("  Read 12 books ", " One a month ", LocalDate.of(2026, 12, 31))

        val goal = repository.goals.value.single()
        assertEquals("Read 12 books", goal.title)
        assertEquals("One a month", goal.description)
        assertEquals(LocalDate.of(2026, 12, 31).atStartOfDay(zone).toInstant(), goal.deadline)
        assertEquals(now, goal.createdAt)
    }
}
