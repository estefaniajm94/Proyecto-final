package com.estef.antiphishingcoach.domain.model

data class IncidentRecord(
    val id: Long,
    val createdAt: Long,
    val title: String?,
    val sourceType: String,
    val sourceApp: String,
    val scenarioType: String?,
    val trafficLight: String,
    val score: Int,
    val sanitizedDomain: String?,
    val recommendationCodes: List<String>,
    val signals: List<DetectedSignal>
)
