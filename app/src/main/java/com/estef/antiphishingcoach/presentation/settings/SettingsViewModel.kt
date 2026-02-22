package com.estef.antiphishingcoach.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleLocalLockUseCase
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
    private val clearLocalDataUseCase: ClearLocalDataUseCase
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
                        "Privacidad extrema activada."
                    } else {
                        "Privacidad extrema desactivada."
                    }
                )
            }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            clearLocalDataUseCase()
            _uiState.update { state ->
                state.copy(statusMessage = "Datos locales eliminados.")
            }
        }
    }

    fun onLocalLockChanged(enabled: Boolean) {
        viewModelScope.launch {
            toggleLocalLockUseCase(enabled)
            _uiState.update { state ->
                state.copy(
                    statusMessage = if (enabled) {
                        "Bloqueo local activado para Historial y Ajustes."
                    } else {
                        "Bloqueo local desactivado."
                    }
                )
            }
        }
    }

    fun onLocalLockNotAvailable() {
        _uiState.update { state ->
            state.copy(
                statusMessage = "No hay biometria o credencial del dispositivo disponible para activar el bloqueo local."
            )
        }
    }

    fun onAccessBlockedByAuthError() {
        _uiState.update { state ->
            state.copy(
                statusMessage = "No se pudo autenticar para abrir Ajustes protegidos."
            )
        }
    }
}
