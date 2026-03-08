package com.estef.antiphishingcoach.presentation.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val latestIncident: LatestIncidentUi? = null,
    val latestTrainingSummary: String = ""
)

data class LatestIncidentUi(
    val incidentId: Long,
    val title: String,
    val createdAtLine: String,
    val score: Int,
    val trafficLightCode: String,
    val sanitizedDomainLine: String?
)
