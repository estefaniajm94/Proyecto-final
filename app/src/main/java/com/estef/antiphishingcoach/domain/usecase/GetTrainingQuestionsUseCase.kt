package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.repository.TrainingRepository

class GetTrainingQuestionsUseCase(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(): List<TrainingQuestion> = repository.getQuestions()
}
