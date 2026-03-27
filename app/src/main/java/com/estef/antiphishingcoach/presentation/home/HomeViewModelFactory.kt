package com.estef.antiphishingcoach.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestIncidentSummaryUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver

class HomeViewModelFactory(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observeLatestIncidentSummaryUseCase: ObserveLatestIncidentSummaryUseCase,
    private val stringResolver: StringResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                observeCurrentUserUseCase,
                observeLatestIncidentSummaryUseCase,
                stringResolver
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
