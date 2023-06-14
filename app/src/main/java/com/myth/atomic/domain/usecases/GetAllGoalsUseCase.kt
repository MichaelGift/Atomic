package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class GetAllGoalsUseCase(private val repository: Repository) {
    fun execute() = repository.getAllGoals()
}