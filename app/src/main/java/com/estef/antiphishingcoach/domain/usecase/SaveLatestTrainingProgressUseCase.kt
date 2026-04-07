package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.repository.TrainingRepository

class SaveLatestTrainingProgressUseCase(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(summary: TrainingProgressSummary) {
        repository.saveLatestProgress(summary)
    }
}
