package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class GetGoalsUseCase(private val repository: Repository) {
    fun execute() = repository.getAllGoals()
}