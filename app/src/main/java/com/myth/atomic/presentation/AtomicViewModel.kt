package com.myth.atomic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.myth.atomic.domain.usecases.AddGoalUseCase
import com.myth.atomic.domain.usecases.AddJournalUseCase
import com.myth.atomic.domain.usecases.GetAllGoalsUseCase
import com.myth.atomic.domain.usecases.GetAllJournalsUseCase
import com.myth.atomic.domain.usecases.GetAllRemindersUseCase
import com.myth.atomic.domain.usecases.GetAllTasksUseCase

class AtomicViewModel(
    private val getAllGoalsUseCase: GetAllGoalsUseCase,
    private val getAllJournalsUseCase: GetAllJournalsUseCase,
    private val getAllRemindersUseCase: GetAllRemindersUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    
) : ViewModel() {
    fun getAllGoals() = liveData {
        val goalsList = getAllGoalsUseCase.execute()
        emit(goalsList)
    }

    fun getAllJournals() = liveData {
        val journalsList = getAllJournalsUseCase.execute()
        emit(journalsList)
    }

    fun getAllReminders() = liveData {
        val remindersList = getAllRemindersUseCase.execute()
        emit(remindersList)
    }

    fun getAllTasks() = liveData {
        val tasksList = getAllTasksUseCase.execute()
        emit(tasksList)
    }
}