package com.myth.atomic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.myth.atomic.domain.usecases.SearchGoalsUseCase
import com.myth.atomic.domain.usecases.SearchJournalsUseCase
import com.myth.atomic.domain.usecases.SearchRemindersUseCase
import com.myth.atomic.domain.usecases.SearchTasksUseCase
import com.myth.atomic.domain.usecases.UpdateGoalUseCase
import com.myth.atomic.domain.usecases.UpdateJournalUseCase
import com.myth.atomic.domain.usecases.UpdateReminderUseCase
import com.myth.atomic.domain.usecases.UpdateTaskUseCase

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