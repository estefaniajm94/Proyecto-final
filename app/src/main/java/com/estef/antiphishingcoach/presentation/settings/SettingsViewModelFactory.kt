package com.estef.antiphishingcoach.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleLocalLockUseCase

class SettingsViewModelFactory(
    private val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    private val observeLocalLockUseCase: ObserveLocalLockUseCase,
    private val toggleExtremePrivacyUseCase: ToggleExtremePrivacyUseCase,
    private val toggleLocalLockUseCase: ToggleLocalLockUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                observeExtremePrivacyUseCase = observeExtremePrivacyUseCase,
                observeLocalLockUseCase = observeLocalLockUseCase,
                toggleExtremePrivacyUseCase = toggleExtremePrivacyUseCase,
                toggleLocalLockUseCase = toggleLocalLockUseCase,
                clearLocalDataUseCase = clearLocalDataUseCase
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
