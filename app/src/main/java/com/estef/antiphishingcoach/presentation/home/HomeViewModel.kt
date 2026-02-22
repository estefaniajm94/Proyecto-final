package com.estef.antiphishingcoach.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestIncidentSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    observeLatestIncidentSummaryUseCase: ObserveLatestIncidentSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            observeLatestIncidentSummaryUseCase().collect { latest ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        latestIncident = latest?.toUi()
                    )
                }
            }
        }
    }

    private fun IncidentSummary.toUi(): LatestIncidentUi {
        return LatestIncidentUi(
            incidentId = incidentId,
            title = title ?: "Analisis sin titulo",
            createdAtLine = "Fecha: ${dateFormat.format(Date(createdAt))}",
            scoreLine = "Score: $score",
            trafficLightLabel = trafficLight
        )
    }
}
