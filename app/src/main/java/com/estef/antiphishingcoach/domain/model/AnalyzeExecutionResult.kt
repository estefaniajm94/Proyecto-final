package com.estef.antiphishingcoach.domain.model

/**
 * Resultado completo de la accion de analizar.
 */
data class AnalyzeExecutionResult(
    val output: AnalysisOutput,
    val persistedIncidentId: Long?,
    val usedExtremePrivacy: Boolean
)
