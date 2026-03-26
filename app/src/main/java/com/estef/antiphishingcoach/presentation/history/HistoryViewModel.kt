package com.estef.antiphishingcoach.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.export.ReportExporter
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveHistoryUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(
    observeHistoryUseCase: ObserveHistoryUseCase,
    observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    private val stringResolver: StringResolver,
    private val reportExporter: ReportExporter = ReportExporter()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private val filterFlow = MutableStateFlow(HistoryTrafficLightFilter.ALL)
    private val sortModeFlow = MutableStateFlow(HistorySortMode.MOST_RECENT)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            combine(
                observeHistoryUseCase(),
                observeExtremePrivacyUseCase(),
                queryFlow,
                filterFlow,
                sortModeFlow
            ) { records, extremePrivacyEnabled, query, filter, sortMode ->
                val filteredRecords = records
                    .filterByQuery(query)
                    .filterByTrafficLight(filter)
                    .sortedByMode(sortMode)

                HistoryUiState(
                    isLoading = false,
                    items = filteredRecords.map { it.toUi() },
                    query = query,
                    selectedTrafficLight = filter,
                    sortMode = sortMode,
                    emptyMessage = buildEmptyMessage(
                        allRecords = records,
                        filteredRecords = filteredRecords,
                        extremePrivacyEnabled = extremePrivacyEnabled
                    )
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun onQueryChanged(query: String) {
        queryFlow.value = query.trim()
    }

    fun onTrafficLightFilterChanged(filter: HistoryTrafficLightFilter) {
        filterFlow.value = filter
    }

    fun onSortModeChanged(mode: HistorySortMode) {
        sortModeFlow.value = mode
    }

    private fun List<IncidentRecord>.filterByQuery(query: String): List<IncidentRecord> {
        if (query.isBlank()) return this
        val normalized = query.lowercase(Locale.getDefault())
        return filter { record ->
            val title = record.title.orEmpty().lowercase(Locale.getDefault())
            val domain = record.sanitizedDomain.orEmpty().lowercase(Locale.getDefault())
            title.contains(normalized) || domain.contains(normalized)
        }
    }

    private fun List<IncidentRecord>.filterByTrafficLight(filter: HistoryTrafficLightFilter): List<IncidentRecord> {
        val expected = filter.rawValue ?: return this
        return filter { record -> record.trafficLight == expected }
    }

    private fun List<IncidentRecord>.sortedByMode(mode: HistorySortMode): List<IncidentRecord> {
        return when (mode) {
            HistorySortMode.MOST_RECENT -> sortedByDescending { it.createdAt }
            HistorySortMode.HIGHEST_RISK -> sortedWith(
                compareByDescending<IncidentRecord> { it.score }
                    .thenByDescending { it.createdAt }
            )
        }
    }

    private fun buildEmptyMessage(
        allRecords: List<IncidentRecord>,
        filteredRecords: List<IncidentRecord>,
        extremePrivacyEnabled: Boolean
    ): String {
        if (filteredRecords.isNotEmpty()) return ""
        return when {
            allRecords.isEmpty() && extremePrivacyEnabled -> {
                stringResolver.get(R.string.history_empty_extreme_privacy)
            }

            allRecords.isEmpty() -> stringResolver.get(R.string.history_empty)
            else -> stringResolver.get(R.string.history_empty_filtered)
        }
    }

    private fun IncidentRecord.toUi(): HistoryItemUi {
        return HistoryItemUi(
            incidentId = id,
            title = title ?: stringResolver.get(R.string.history_item_title_fallback),
            metaLine = stringResolver.get(R.string.history_item_meta, sourceApp),
            createdAtLine = stringResolver.get(R.string.history_item_created, dateFormat.format(Date(createdAt))),
            trafficLight = trafficLight,
            score = score,
            sourceApp = sourceApp,
            sanitizedDomain = sanitizedDomain,
            createdAtMillis = createdAt,
            signalTags = reportExporter
                .buildSignalTags(signals)
                .ifEmpty { recommendationCodes.take(2) }
        )
    }
}
