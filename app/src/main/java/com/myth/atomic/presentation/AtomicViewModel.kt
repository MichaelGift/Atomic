package com.myth.atomic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.myth.atomic.data.model.Goals
import com.myth.atomic.data.model.Journal
import com.myth.atomic.data.model.Reminder
import com.myth.atomic.data.model.Task
import com.myth.atomic.domain.usecases.AddGoalUseCase
import com.myth.atomic.domain.usecases.AddJournalUseCase
import com.myth.atomic.domain.usecases.AddReminderUseCase
import com.myth.atomic.domain.usecases.AddTaskUseCase
import com.myth.atomic.domain.usecases.GetAllGoalsUseCase
import com.myth.atomic.domain.usecases.GetAllJournalsUseCase
import com.myth.atomic.domain.usecases.GetAllRemindersUseCase
import com.myth.atomic.domain.usecases.GetAllTasksUseCase

class AtomicViewModel(
    private val getAllGoalsUseCase: GetAllGoalsUseCase,
    private val getAllJournalsUseCase: GetAllJournalsUseCase,
    private val getAllRemindersUseCase: GetAllRemindersUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val addJournalUseCase: AddJournalUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val addTaskUseCase: AddTaskUseCase,
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

    fun AddGoal(goal: Goals) = viewModelScope.launch{
        addGoalUseCase.execute(goal)
    }
    fun AddJournalEntry(journal: Journal) = viewModelScope.launch {
        addJournalUseCase.execute(journal)
    }
    fun AddReminder(reminder: Reminder) =viewModelScope.launch {
        addReminderUseCase.execute(reminder)
    }
    fun AddTask(task: Task) =viewModelScope.launch {
        addTaskUseCase.execute(task)
    }

}