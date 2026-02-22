package com.estef.antiphishingcoach.domain.repository

import com.estef.antiphishingcoach.domain.model.TrainingQuestion

interface TrainingRepository {
    suspend fun getQuestions(): List<TrainingQuestion>
}
