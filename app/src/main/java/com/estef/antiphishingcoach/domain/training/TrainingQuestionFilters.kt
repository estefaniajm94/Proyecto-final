package com.estef.antiphishingcoach.domain.training

import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingQuestion

fun List<TrainingQuestion>.filterByLevel(level: TrainingLevel): List<TrainingQuestion> {
    return filter { question -> question.level == level }
}
