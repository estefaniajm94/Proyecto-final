package com.estef.antiphishingcoach.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.RegisterLocalUserUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver

class RegisterViewModelFactory(
    private val registerLocalUserUseCase: RegisterLocalUserUseCase,
    private val stringResolver: StringResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(registerLocalUserUseCase, stringResolver) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
