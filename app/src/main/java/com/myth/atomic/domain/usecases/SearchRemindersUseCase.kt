package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class SearchRemindersUseCase(val repository: Repository) {
    fun execute(query: String) = repository.searchReminder(query)
}