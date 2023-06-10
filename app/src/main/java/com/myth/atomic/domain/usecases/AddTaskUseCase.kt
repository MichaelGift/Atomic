package com.myth.atomic.domain.usecases

import com.myth.atomic.data.model.Task
import com.myth.atomic.domain.repository.Repository

class AddTaskUseCase(private val repository: Repository) {
    suspend fun execute(task: Task)=repository.addTask(task)
}