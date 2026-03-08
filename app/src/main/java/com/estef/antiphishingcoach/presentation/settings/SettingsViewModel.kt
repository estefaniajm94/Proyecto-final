package com.estef.antiphishingcoach.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleLocalLockUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    observeLocalLockUseCase: ObserveLocalLockUseCase,
    private val toggleExtremePrivacyUseCase: ToggleExtremePrivacyUseCase,
    private val toggleLocalLockUseCase: ToggleLocalLockUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
    private val stringResolver: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeExtremePrivacyUseCase().collect { enabled ->
                _uiState.update { state -> state.copy(extremePrivacyEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            observeLocalLockUseCase().collect { enabled ->
                _uiState.update { state -> state.copy(localLockEnabled = enabled) }
            }
        }
    }

    fun onExtremePrivacyChanged(enabled: Boolean) {
        viewModelScope.launch {
            toggleExtremePrivacyUseCase(enabled)
            _uiState.update { state ->
                state.copy(
                    statusMessage = if (enabled) {
                        stringResolver.get(R.string.settings_extreme_privacy_on)
                    } else {
                        stringResolver.get(R.string.settings_extreme_privacy_off)
                    }
                )
            }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            clearLocalDataUseCase()
            _uiState.update { state ->
                state.copy(statusMessage = stringResolver.get(R.string.settings_data_cleared))
            }
        }
    }

    fun onLocalLockChanged(enabled: Boolean) {
        viewModelScope.launch {
            toggleLocalLockUseCase(enabled)
            _uiState.update { state ->
                state.copy(
                    statusMessage = if (enabled) {
                        stringResolver.get(R.string.settings_local_lock_on)
                    } else {
                        stringResolver.get(R.string.settings_local_lock_off)
                    }
                )
            }
        }
    }

    fun onLocalLockNotAvailable() {
        _uiState.update { state ->
            state.copy(
                statusMessage = stringResolver.get(R.string.settings_biometric_not_available)
            )
        }
    }

    fun onAccessBlockedByAuthError() {
        _uiState.update { state ->
            state.copy(
                statusMessage = stringResolver.get(R.string.settings_auth_error)
            )
        }
    }
}
