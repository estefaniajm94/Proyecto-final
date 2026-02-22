package com.estef.antiphishingcoach.presentation.history

data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<HistoryItemUi> = emptyList()
)

data class HistoryItemUi(
    val incidentId: Long,
    val title: String,
    val metaLine: String,
    val createdAtLine: String
)
