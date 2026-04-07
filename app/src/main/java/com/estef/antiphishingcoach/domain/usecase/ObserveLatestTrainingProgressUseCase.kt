package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow

class ObserveLatestTrainingProgressUseCase(
    private val repository: TrainingRepository
) {
    operator fun invoke(): Flow<TrainingProgressSummary?> = repository.observeLatestProgress()
}
