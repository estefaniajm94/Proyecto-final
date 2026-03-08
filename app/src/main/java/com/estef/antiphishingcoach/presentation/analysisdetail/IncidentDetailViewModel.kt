package com.estef.antiphishingcoach.presentation.analysisdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.core.export.ReportExporter
import com.estef.antiphishingcoach.domain.model.RecommendationCatalog
import com.estef.antiphishingcoach.data.export.ExportedReportFile
import com.estef.antiphishingcoach.data.export.ExportReportToFileUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveIncidentDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentDetailViewModel(
    incidentId: Long,
    observeIncidentDetailUseCase: ObserveIncidentDetailUseCase,
    private val exportReportToFileUseCase: ExportReportToFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentDetailUiState())
    val uiState: StateFlow<IncidentDetailUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val reportExporter = ReportExporter()

    init {
        viewModelScope.launch {
            observeIncidentDetailUseCase(incidentId).collect { incident ->
                if (incident == null) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            notFound = true,
                            incident = null,
                            recommendations = emptyList(),
                            createdAtText = ""
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            notFound = false,
                            incident = incident,
                            recommendations = RecommendationCatalog.fromCodes(incident.recommendationCodes),
                            createdAtText = dateFormat.format(Date(incident.createdAt))
                        )
                    }
                }
            }
        }
    }

    fun buildMarkdownReport(): String? {
        val incident = _uiState.value.incident ?: return null
        return reportExporter.buildMarkdown(incident)
    }

    fun exportMarkdownReportFile(): ExportedReportFile? {
        val incident = _uiState.value.incident ?: return null
        return runCatching {
            exportReportToFileUseCase(incident)
        }.getOrNull()
    }
}
