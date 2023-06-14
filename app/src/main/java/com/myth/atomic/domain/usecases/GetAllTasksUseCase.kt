package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class GetAllTasksUseCase(val repository: Repository) {
    fun execute()=repository.getAllTasks()
}