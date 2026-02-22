package com.estef.antiphishingcoach.domain.model

import com.estef.antiphishingcoach.core.model.ScenarioType
import com.estef.antiphishingcoach.core.model.SourceApp

/**
 * Entrada de analisis para el MVP A.
 */
data class AnalyzeRequest(
    val inputText: String,
    val title: String?,
    val sourceApp: SourceApp,
    val scenarioType: ScenarioType? = null
)
