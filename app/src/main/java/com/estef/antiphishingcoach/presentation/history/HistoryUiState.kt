package com.estef.antiphishingcoach.presentation.history

data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<HistoryItemUi> = emptyList(),
    val query: String = "",
    val selectedTrafficLight: HistoryTrafficLightFilter = HistoryTrafficLightFilter.ALL,
    val sortMode: HistorySortMode = HistorySortMode.MOST_RECENT,
    val emptyMessage: String = ""
)

enum class HistoryTrafficLightFilter(val rawValue: String?) {
    ALL(null),
    GREEN("GREEN"),
    YELLOW("YELLOW"),
    RED("RED")
}

enum class HistorySortMode {
    MOST_RECENT,
    HIGHEST_RISK
}

data class HistoryItemUi(
    val incidentId: Long,
    val title: String,
    val metaLine: String,
    val createdAtLine: String,
    val trafficLight: String,
    val score: Int,
    val sourceApp: String,
    val sanitizedDomain: String?,
    val createdAtMillis: Long,
    val signalTags: List<String>
)
