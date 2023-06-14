package com.myth.atomic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.myth.atomic.data.model.Goals
import com.myth.atomic.data.model.Journal
import com.myth.atomic.data.model.Reminder
import com.myth.atomic.data.model.Task
import com.myth.atomic.domain.usecases.AddGoalUseCase
import com.myth.atomic.domain.usecases.AddJournalUseCase
import com.myth.atomic.domain.usecases.AddReminderUseCase
import com.myth.atomic.domain.usecases.AddTaskUseCase
import com.myth.atomic.domain.usecases.DeleteGoalsUseCase
import com.myth.atomic.domain.usecases.DeleteJournalUseCase
import com.myth.atomic.domain.usecases.DeleteReminderUseCase
import com.myth.atomic.domain.usecases.DeleteTaskUseCase
import com.myth.atomic.domain.usecases.GetAllGoalsUseCase
import com.myth.atomic.domain.usecases.GetAllJournalsUseCase
import com.myth.atomic.domain.usecases.GetAllRemindersUseCase
import com.myth.atomic.domain.usecases.GetAllTasksUseCase
import com.myth.atomic.domain.usecases.UpdateGoalUseCase
import com.myth.atomic.domain.usecases.UpdateJournalUseCase
import com.myth.atomic.domain.usecases.UpdateReminderUseCase
import com.myth.atomic.domain.usecases.UpdateTaskUseCase
import kotlinx.coroutines.launch

class AtomicViewModel(
    private val getAllGoalsUseCase: GetAllGoalsUseCase,
    private val getAllJournalsUseCase: GetAllJournalsUseCase,
    private val getAllRemindersUseCase: GetAllRemindersUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val addJournalUseCase: AddJournalUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val updateJournalUseCase: UpdateJournalUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteGoalsUseCase: DeleteGoalsUseCase,
    private val deleteJournalUseCase: DeleteJournalUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {
    fun GetAllGoals() = liveData {
        val goalsList = getAllGoalsUseCase.execute()
        emit(goalsList)
    }
    fun GetAllJournals() = liveData {
        val journalsList = getAllJournalsUseCase.execute()
        emit(journalsList)
    }
    fun GetAllReminders() = liveData {
        val remindersList = getAllRemindersUseCase.execute()
        emit(remindersList)
    }
    fun GetAllTasks() = liveData {
        val tasksList = getAllTasksUseCase.execute()
        emit(tasksList)
    }

    fun AddGoal(goal: Goals) = viewModelScope.launch {
        addGoalUseCase.execute(goal)
    }
    fun AddJournalEntry(journal: Journal) = viewModelScope.launch {
        addJournalUseCase.execute(journal)
    }
    fun AddReminder(reminder: Reminder) = viewModelScope.launch {
        addReminderUseCase.execute(reminder)
    }
    fun AddTask(task: Task) = viewModelScope.launch {
        addTaskUseCase.execute(task)
    }

    fun UpdateGoal(goal: Goals) = viewModelScope.launch {
        updateGoalUseCase.execute(goal)
    }
    fun UpdateJournal(journal: Journal) = viewModelScope.launch {
        updateJournalUseCase.execute(journal)
    }
    fun UpdateReminder(reminder: Reminder) = viewModelScope.launch {
        updateReminderUseCase.execute(reminder)
    }
    fun UpdateTask(task: Task) = viewModelScope.launch {
        updateTaskUseCase.execute(task)
    }

    fun DeleteGoal(goal: Goals) = viewModelScope.launch {
        deleteGoalsUseCase.execute(goal)
    }
    fun DeleteJournal(journal: Journal) = viewModelScope.launch {
        deleteJournalUseCase.execute(journal)
    }
    fun DeleteReminder(reminder: Reminder) = viewModelScope.launch {
        deleteReminderUseCase.execute(reminder)
    }
    fun DeleteTask(task: Task) = viewModelScope.launch {
        deleteTaskUseCase.execute(task)
    }
}