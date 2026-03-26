package com.estef.antiphishingcoach.presentation.analyze

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.RecommendationItem

data class AnalyzeUiState(
    val isLoading: Boolean = false,
    val extremePrivacyEnabled: Boolean = false,
    val inputError: String? = null,
    val statusMessage: String? = null,
    val flowState: AnalyzeFlowState = AnalyzeFlowState.Idle,
    val result: AnalysisPresentation? = null
)

sealed interface AnalyzeFlowState {
    data object Idle : AnalyzeFlowState
    data object PickingImage : AnalyzeFlowState
    data object OcrRunning : AnalyzeFlowState
    data class OcrReady(val text: String) : AnalyzeFlowState
    data object Analyzing : AnalyzeFlowState
    data object ResultReady : AnalyzeFlowState
    data class Error(val message: String) : AnalyzeFlowState
}

data class AnalysisPresentation(
    val analyzedInput: String,
    val score: Int,
    val trafficLight: TrafficLight,
    val sourceTypeLabel: String,
    val sanitizedDomain: String?,
    val quickExplanation: String,
    val urlInsights: List<AnalyzeUrlInsight>,
    val suspiciousPhrases: List<SuspiciousPhraseInsight>,
    val actionPlan: AnalyzeActionPlan,
    val signals: List<DetectedSignal>,
    val recommendations: List<RecommendationItem>,
    val persistedIncidentId: Long?
)
