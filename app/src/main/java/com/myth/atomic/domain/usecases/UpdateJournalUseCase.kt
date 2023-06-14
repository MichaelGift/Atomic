package com.myth.atomic.domain.usecases

import com.myth.atomic.data.model.Journal
import com.myth.atomic.domain.repository.Repository

class UpdateJournalUseCase(private val repository: Repository) {
    suspend fun execute(journal: Journal)= repository.updateJournal(journal)
}