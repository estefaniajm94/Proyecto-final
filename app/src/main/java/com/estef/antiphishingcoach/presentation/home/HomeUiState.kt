package com.estef.antiphishingcoach.presentation.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val latestIncident: LatestIncidentUi? = null,
    val latestTrainingSummary: String = "No hay progreso guardado de entrenamiento en este MVP."
)

data class LatestIncidentUi(
    val incidentId: Long,
    val title: String,
    val createdAtLine: String,
    val scoreLine: String,
    val trafficLightLabel: String
)
