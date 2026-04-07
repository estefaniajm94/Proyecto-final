package com.estef.antiphishingcoach.domain.repository

import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    suspend fun getQuestions(level: TrainingLevel? = null): List<TrainingQuestion>
    fun observeLatestProgress(): Flow<TrainingProgressSummary?>
    suspend fun saveLatestProgress(summary: TrainingProgressSummary)
    suspend fun clearLatestProgress()
}
