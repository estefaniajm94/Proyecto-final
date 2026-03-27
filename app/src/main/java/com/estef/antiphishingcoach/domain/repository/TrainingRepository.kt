package com.estef.antiphishingcoach.domain.repository

import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingQuestion

interface TrainingRepository {
    suspend fun getQuestions(level: TrainingLevel? = null): List<TrainingQuestion>
}
