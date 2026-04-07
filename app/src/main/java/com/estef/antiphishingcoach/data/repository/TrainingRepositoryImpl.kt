package com.estef.antiphishingcoach.data.repository

import com.estef.antiphishingcoach.data.local.preferences.SecureSettingsDataSource
import com.estef.antiphishingcoach.data.local.seed.SeedAssetLoader
import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.repository.TrainingRepository
import com.estef.antiphishingcoach.domain.training.filterByLevel
import kotlinx.coroutines.flow.Flow

class TrainingRepositoryImpl(
    private val seedLoader: SeedAssetLoader,
    private val secureSettingsDataSource: SecureSettingsDataSource
) : TrainingRepository {
    override suspend fun getQuestions(level: TrainingLevel?): List<TrainingQuestion> {
        val questions = seedLoader.loadTrainingQuestions().map { dto ->
            TrainingQuestion(
                id = dto.id,
                prompt = dto.prompt,
                options = dto.options,
                correctIndex = dto.correctIndex,
                explanation = dto.explanation,
                level = TrainingLevel.fromRaw(dto.level),
                category = dto.category
                    ?.trim()
                    ?.takeIf { category -> category.isNotEmpty() }
                    ?: TrainingQuestion.DEFAULT_CATEGORY
            )
        }
        return level?.let(questions::filterByLevel) ?: questions
    }

    override fun observeLatestProgress(): Flow<TrainingProgressSummary?> {
        return secureSettingsDataSource.observeLatestTrainingProgress()
    }

    override suspend fun saveLatestProgress(summary: TrainingProgressSummary) {
        secureSettingsDataSource.setLatestTrainingProgress(summary)
    }

    override suspend fun clearLatestProgress() {
        secureSettingsDataSource.setLatestTrainingProgress(null)
    }
}
