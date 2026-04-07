package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.repository.TrainingRepository

class ClearLocalDataUseCase(
    private val incidentRepository: IncidentRepository,
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke() {
        incidentRepository.clearAll()
        trainingRepository.clearLatestProgress()
    }
}
