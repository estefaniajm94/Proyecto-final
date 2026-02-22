package com.estef.antiphishingcoach.domain.model

import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.core.model.SourceType

data class AnalysisOutput(
    val score: Int,
    val trafficLight: TrafficLight,
    val sourceType: SourceType,
    val sanitizedDomain: String?,
    val recommendationCodes: List<String>,
    val signals: List<DetectedSignal>
)
