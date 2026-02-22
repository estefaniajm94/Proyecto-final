package com.estef.antiphishingcoach.presentation.analysisdetail

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.RecommendationItem

data class IncidentDetailUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val incident: IncidentRecord? = null,
    val recommendations: List<RecommendationItem> = emptyList(),
    val createdAtText: String = ""
)
