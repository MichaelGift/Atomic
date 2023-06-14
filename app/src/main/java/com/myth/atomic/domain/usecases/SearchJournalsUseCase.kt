package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class SearchJournalsUseCase(val repository: Repository) {
    fun execute(query: String)= repository.searchJournal(query)
}