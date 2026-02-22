package com.estef.antiphishingcoach.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestIncidentSummaryUseCase

class HomeViewModelFactory(
    private val observeLatestIncidentSummaryUseCase: ObserveLatestIncidentSummaryUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(observeLatestIncidentSummaryUseCase) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
