package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class SearchTasksUseCase(val repository: Repository) {
    fun execute(query: Int) = repository.searchTask(query)
}