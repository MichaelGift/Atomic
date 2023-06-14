package com.myth.atomic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.myth.atomic.data.model.*
import com.myth.atomic.domain.usecases.*
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
    private val searchGoalsUseCase: SearchGoalsUseCase,
    private val searchJournalsUseCase: SearchJournalsUseCase,
    private val searchRemindersUseCase: SearchRemindersUseCase,
    private val searchTasksUseCase: SearchTasksUseCase,
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

    fun SearchGoal(query: String) = viewModelScope.launch {
        searchGoalsUseCase.execute(query)
    }
    fun SearchJournal(query: String) = viewModelScope.launch {
        searchJournalsUseCase.execute(query)
    }
    fun SearchReminders(query: String) = viewModelScope.launch {
        searchRemindersUseCase.execute(query)
    }
    fun SearchTask(query: Int) = viewModelScope.launch {
        searchTasksUseCase.execute(query)
    }
}