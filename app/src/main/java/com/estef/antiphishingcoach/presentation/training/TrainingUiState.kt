package com.estef.antiphishingcoach.presentation.training

import com.estef.antiphishingcoach.domain.model.TrainingQuestion

data class TrainingUiState(
    val isLoading: Boolean = true,
    val totalQuestions: Int = 0,
    val currentPosition: Int = 0,
    val currentQuestion: TrainingQuestion? = null,
    val score: Int = 0,
    val answerChecked: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val lastExplanation: String? = null,
    val completed: Boolean = false
)
