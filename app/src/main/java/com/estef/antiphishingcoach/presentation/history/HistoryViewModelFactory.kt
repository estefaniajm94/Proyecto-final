package com.estef.antiphishingcoach.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveHistoryUseCase

class HistoryViewModelFactory(
    private val observeHistoryUseCase: ObserveHistoryUseCase,
    private val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                observeHistoryUseCase = observeHistoryUseCase,
                observeExtremePrivacyUseCase = observeExtremePrivacyUseCase
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
