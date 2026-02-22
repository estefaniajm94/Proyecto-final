package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.IncidentRepository

class ClearLocalDataUseCase(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke() {
        repository.clearAll()
    }
}
