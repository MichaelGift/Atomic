package com.myth.atomic.presentation.journal

import androidx.lifecycle.SavedStateHandle
import com.myth.atomic.domain.model.Goal
import com.myth.atomic.domain.model.JournalEntry
import com.myth.atomic.presentation.journal.editor.JournalEditorViewModel
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class JournalViewModelsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-03-14T09:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val repository = FakeAtomicRepository(clock)

    @Test
    fun journalListSearchesAndShowsGoalTitles() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        repository.addEntry(JournalEntry(title = "First run", body = "Legs hurt", createdAt = now, goalId = goalId))
        repository.addEntry(JournalEntry(title = "Reading", body = "Finished a book", createdAt = now.minus(Duration.ofDays(1))))

        val viewModel = JournalViewModel(repository, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(listOf("Run a 10k", null), viewModel.uiState.value.entries.map { it.goalTitle })

        viewModel.onQueryChange("book")
        assertTrue(viewModel.uiState.value.isSearching)
        assertEquals(listOf("Reading"), viewModel.uiState.value.entries.map { it.entry.title })
    }

    @Test
    fun newEntryFromGoalIsLinkedAndSaved() = runTest {
        val goalId = repository.addGoal(Goal(title = "Run a 10k", createdAt = now))
        val viewModel = editor(goalId = goalId)
        assertFalse(viewModel.uiState.canSave)

        viewModel.onBodyChange("  Ran 3k without stopping ")
        viewModel.save()

        val entry = repository.entries.value.single()
        assertEquals("Ran 3k without stopping", entry.body)
        assertEquals(goalId, entry.goalId)
        assertEquals(now, entry.createdAt)
        assertTrue(viewModel.uiState.isDone)
    }

    @Test
    fun editingEntryKeepsCreationTime() = runTest {
        val written = now.minus(Duration.ofDays(3))
        val entryId = repository.addEntry(JournalEntry(title = "Draft", body = "Old text", createdAt = written))
        val viewModel = editor(entryId = entryId)
        assertEquals("Old text", viewModel.uiState.body)

        viewModel.onTitleChange("Final")
        viewModel.save()

        val entry = repository.entries.value.single()
        assertEquals("Final", entry.title)
        assertEquals(written, entry.createdAt)
    }

    @Test
    fun deletingEntryClosesEditor() = runTest {
        val entryId = repository.addEntry(JournalEntry(title = "Draft", body = "Old text", createdAt = now))
        val viewModel = editor(entryId = entryId)

        viewModel.delete()

        assertTrue(repository.entries.value.isEmpty())
        assertTrue(viewModel.uiState.isDone)
    }

    private fun editor(entryId: Long = 0, goalId: Long = 0) = JournalEditorViewModel(
        SavedStateHandle(mapOf("entryId" to entryId, "goalId" to goalId)),
        repository,
        repository,
        clock,
    )
}
