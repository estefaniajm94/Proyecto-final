package com.estef.antiphishingcoach.presentation.coach

data class ScenarioChecklistUiState(
    val isLoading: Boolean = true,
    val scenarioTitle: String = "",
    val items: List<String> = emptyList(),
    val markedCount: Int = 0
)
