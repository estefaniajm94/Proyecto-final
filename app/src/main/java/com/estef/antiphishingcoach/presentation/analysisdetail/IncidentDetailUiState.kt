package com.estef.antiphishingcoach.presentation.analysisdetail

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.RecommendationItem
import com.estef.antiphishingcoach.presentation.analyze.AnalyzeActionPlan

data class IncidentDetailUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val incident: IncidentRecord? = null,
    val recommendations: List<RecommendationItem> = emptyList(),
    val actionPlan: AnalyzeActionPlan = AnalyzeActionPlan(emptyList(), false),
    val createdAtText: String = ""
)
