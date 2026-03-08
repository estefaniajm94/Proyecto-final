package com.estef.antiphishingcoach.presentation.analysisdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.data.export.ExportReportToFileUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveIncidentDetailUseCase

class IncidentDetailViewModelFactory(
    private val incidentId: Long,
    private val observeIncidentDetailUseCase: ObserveIncidentDetailUseCase,
    private val exportReportToFileUseCase: ExportReportToFileUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncidentDetailViewModel(
                incidentId = incidentId,
                observeIncidentDetailUseCase = observeIncidentDetailUseCase,
                exportReportToFileUseCase = exportReportToFileUseCase
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
