package com.estef.antiphishingcoach.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.LogoutCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase
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
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
    observeLocalLockUseCase: ObserveLocalLockUseCase,
    private val toggleExtremePrivacyUseCase: ToggleExtremePrivacyUseCase,
    private val toggleLocalLockUseCase: ToggleLocalLockUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
    private val logoutCurrentUserUseCase: LogoutCurrentUserUseCase,
    private val stringResolver: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update { state ->
                    state.copy(
                        currentUserName = user?.displayName,
                        currentUserEmail = user?.email
                    )
                }
            }
        }
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
                    logoutCompleted = false,
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

    fun logout() {
        viewModelScope.launch {
            logoutCurrentUserUseCase()
            _uiState.update { state ->
                state.copy(
                    logoutCompleted = true,
                    statusMessage = stringResolver.get(R.string.settings_logout_done)
                )
            }
        }
    }

    fun onLocalLockChanged(enabled: Boolean) {
        viewModelScope.launch {
            toggleLocalLockUseCase(enabled)
            _uiState.update { state ->
                state.copy(
                    logoutCompleted = false,
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
                logoutCompleted = false,
                statusMessage = stringResolver.get(R.string.settings_biometric_not_available)
            )
        }
    }

    fun onAccessBlockedByAuthError() {
        _uiState.update { state ->
            state.copy(
                logoutCompleted = false,
                statusMessage = stringResolver.get(R.string.settings_auth_error)
            )
        }
    }
}
