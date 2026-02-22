package com.estef.antiphishingcoach.data.repository

import com.estef.antiphishingcoach.data.local.seed.SeedAssetLoader
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.repository.TrainingRepository

class TrainingRepositoryImpl(
    private val seedLoader: SeedAssetLoader
) : TrainingRepository {
    override suspend fun getQuestions(): List<TrainingQuestion> {
        return seedLoader.loadTrainingQuestions().map { dto ->
            TrainingQuestion(
                id = dto.id,
                prompt = dto.prompt,
                options = dto.options,
                correctIndex = dto.correctIndex,
                explanation = dto.explanation
            )
        }
    }
}
