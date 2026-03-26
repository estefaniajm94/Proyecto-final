package com.estef.antiphishingcoach.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.LoginLocalUserUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver

class LoginViewModelFactory(
    private val loginLocalUserUseCase: LoginLocalUserUseCase,
    private val stringResolver: StringResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(loginLocalUserUseCase, stringResolver) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
