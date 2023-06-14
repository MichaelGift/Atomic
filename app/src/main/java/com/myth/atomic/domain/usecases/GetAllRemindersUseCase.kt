package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class GetAllRemindersUseCase(val repository: Repository) {
    fun execute() = repository.getAllReminders()
}