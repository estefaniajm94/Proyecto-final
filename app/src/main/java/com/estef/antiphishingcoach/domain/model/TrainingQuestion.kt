package com.estef.antiphishingcoach.domain.model

data class TrainingQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
