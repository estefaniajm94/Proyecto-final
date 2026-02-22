package com.estef.antiphishingcoach.domain.model

/**
 * Resumen ligero para dashboard/Home sin cargar detalle completo.
 */
data class IncidentSummary(
    val incidentId: Long,
    val createdAt: Long,
    val title: String?,
    val sourceApp: String,
    val trafficLight: String,
    val score: Int
)
