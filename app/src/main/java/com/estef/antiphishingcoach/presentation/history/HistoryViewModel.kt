package com.estef.antiphishingcoach.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(
    observeHistoryUseCase: ObserveHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            observeHistoryUseCase().collect { records ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        items = records.map { record -> record.toUi() }
                    )
                }
            }
        }
    }

    private fun IncidentRecord.toUi(): HistoryItemUi {
        return HistoryItemUi(
            incidentId = id,
            title = title ?: "Analisis sin titulo",
            metaLine = "Score $score | $trafficLight | $sourceApp",
            createdAtLine = "Fecha: ${dateFormat.format(Date(createdAt))}"
        )
    }
}
