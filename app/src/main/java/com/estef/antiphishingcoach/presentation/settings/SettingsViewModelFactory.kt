package com.estef.antiphishingcoach.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.LogoutCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleLocalLockUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver

class SettingsViewModelFactory(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    private val observeLocalLockUseCase: ObserveLocalLockUseCase,
    private val toggleExtremePrivacyUseCase: ToggleExtremePrivacyUseCase,
    private val toggleLocalLockUseCase: ToggleLocalLockUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
    private val logoutCurrentUserUseCase: LogoutCurrentUserUseCase,
    private val stringResolver: StringResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                observeCurrentUserUseCase = observeCurrentUserUseCase,
                observeExtremePrivacyUseCase = observeExtremePrivacyUseCase,
                observeLocalLockUseCase = observeLocalLockUseCase,
                toggleExtremePrivacyUseCase = toggleExtremePrivacyUseCase,
                toggleLocalLockUseCase = toggleLocalLockUseCase,
                clearLocalDataUseCase = clearLocalDataUseCase,
                logoutCurrentUserUseCase = logoutCurrentUserUseCase,
                stringResolver = stringResolver
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
