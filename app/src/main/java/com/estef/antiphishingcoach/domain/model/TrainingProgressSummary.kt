package com.estef.antiphishingcoach.domain.model

data class TrainingProgressSummary(
    val level: TrainingLevel,
    val score: Int,
    val totalQuestions: Int,
    val completedAt: Long
)
