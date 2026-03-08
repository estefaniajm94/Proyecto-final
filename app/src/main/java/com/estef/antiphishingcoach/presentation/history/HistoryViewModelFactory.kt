package com.estef.antiphishingcoach.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveHistoryUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver

class HistoryViewModelFactory(
    private val observeHistoryUseCase: ObserveHistoryUseCase,
    private val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    private val stringResolver: StringResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                observeHistoryUseCase = observeHistoryUseCase,
                observeExtremePrivacyUseCase = observeExtremePrivacyUseCase,
                stringResolver = stringResolver
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
