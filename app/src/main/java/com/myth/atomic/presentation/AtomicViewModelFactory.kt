package com.myth.atomic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myth.atomic.domain.usecases.*

class AtomicViewModelFactory(
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
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AtomicViewModel(
            getAllGoalsUseCase,
            getAllJournalsUseCase,
            getAllRemindersUseCase,
            getAllTasksUseCase,
            addGoalUseCase,
            addJournalUseCase,
            addReminderUseCase,
            addTaskUseCase,
            updateGoalUseCase,
            updateJournalUseCase,
            updateReminderUseCase,
            updateTaskUseCase,
            deleteGoalsUseCase,
            deleteJournalUseCase,
            deleteReminderUseCase,
            deleteTaskUseCase,
            searchGoalsUseCase,
            searchJournalsUseCase,
            searchRemindersUseCase,
            searchTasksUseCase
        ) as T
    }
}