package com.myth.atomic.domain.usecases

import com.myth.atomic.domain.repository.Repository

class GetAllJournalsUseCase(val repository: Repository) {
    fun execute()=repository.getAllJournals()
}