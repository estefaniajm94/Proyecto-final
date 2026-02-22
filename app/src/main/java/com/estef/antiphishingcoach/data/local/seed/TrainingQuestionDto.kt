package com.estef.antiphishingcoach.data.local.seed

data class TrainingQuestionDto(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
