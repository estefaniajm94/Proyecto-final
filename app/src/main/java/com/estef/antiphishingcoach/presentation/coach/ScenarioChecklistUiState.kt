package com.estef.antiphishingcoach.presentation.coach

import com.estef.antiphishingcoach.domain.model.CoachScenario

data class ScenarioChecklistUiState(
    val isLoading: Boolean = true,
    val scenario: CoachScenario? = null,
    val markedCount: Int = 0
)
