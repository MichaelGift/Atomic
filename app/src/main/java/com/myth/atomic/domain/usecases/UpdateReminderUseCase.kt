package com.myth.atomic.domain.usecases

import com.myth.atomic.data.model.Reminder
import com.myth.atomic.domain.repository.Repository

class UpdateReminderUseCase(private val repository: Repository) {
    suspend fun execute(reminder: Reminder)= repository.updateReminder(reminder)
}