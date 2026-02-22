package com.estef.antiphishingcoach.presentation.coach

import com.estef.antiphishingcoach.domain.model.CoachScenario

data class CoachUiState(
    val isLoading: Boolean = true,
    val scenarios: List<CoachScenario> = emptyList()
)
