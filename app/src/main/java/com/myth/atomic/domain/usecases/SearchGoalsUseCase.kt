package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class SearchGoalsUseCase(val repository: Repository) {
    fun execute(query: String) = repository.searchGoal(query)
}