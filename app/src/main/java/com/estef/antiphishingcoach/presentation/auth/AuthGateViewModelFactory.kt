package com.estef.antiphishingcoach.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase

class AuthGateViewModelFactory(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthGateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthGateViewModel(observeCurrentUserUseCase) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
