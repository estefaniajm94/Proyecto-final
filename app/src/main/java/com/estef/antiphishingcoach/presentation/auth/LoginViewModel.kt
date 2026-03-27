package com.estef.antiphishingcoach.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.core.avatar.AvatarCatalog
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.model.AuthErrorReason
import com.estef.antiphishingcoach.domain.usecase.FindUserByEmailUseCase
import com.estef.antiphishingcoach.domain.usecase.LoginLocalUserUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

class LoginViewModel(
    private val loginLocalUserUseCase: LoginLocalUserUseCase,
    private val findUserByEmailUseCase: FindUserByEmailUseCase,
    private val stringResolver: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(
            previewAvatarId = AvatarCatalog.DEFAULT_AVATAR_ID,
            previewMessage = stringResolver.get(R.string.login_avatar_default_message)
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private var previewJob: Job? = null

    fun onEmailChanged(email: String) {
        val normalizedEmail = email.trim()
        if (!EMAIL_REGEX.matches(normalizedEmail)) {
            resetPreview()
            return
        }

        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            val user = findUserByEmailUseCase(normalizedEmail)
            _uiState.update { state ->
                state.copy(
                    previewAvatarId = user?.avatarId ?: AvatarCatalog.DEFAULT_AVATAR_ID,
                    previewMessage = if (user != null) {
                        stringResolver.get(R.string.login_avatar_known_message, user.displayName)
                    } else {
                        stringResolver.get(R.string.login_avatar_default_message)
                    }
                )
            }
        }
    }

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()

        val emailError = when {
            normalizedEmail.isBlank() -> stringResolver.get(R.string.auth_error_email_required)
            !EMAIL_REGEX.matches(normalizedEmail) -> stringResolver.get(R.string.auth_error_email_invalid)
            else -> null
        }
        val passwordError = when {
            normalizedPassword.isBlank() -> stringResolver.get(R.string.auth_error_password_required)
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.update { state ->
                state.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    statusMessage = null,
                    authenticatedUser = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    statusMessage = null,
                    authenticatedUser = null
                )
            }

            when (val result = loginLocalUserUseCase(normalizedEmail, normalizedPassword)) {
                is AuthActionResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            authenticatedUser = result.user,
                            statusMessage = null
                        )
                    }
                }

                is AuthActionResult.Error -> {
                    val message = when (result.reason) {
                        AuthErrorReason.INVALID_CREDENTIALS -> stringResolver.get(R.string.login_error_invalid_credentials)
                        AuthErrorReason.EMAIL_ALREADY_IN_USE -> stringResolver.get(R.string.auth_error_generic)
                    }
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            statusMessage = message,
                            authenticatedUser = null
                        )
                    }
                }
            }
        }
    }

    private fun resetPreview() {
        previewJob?.cancel()
        _uiState.update { state ->
            state.copy(
                previewAvatarId = AvatarCatalog.DEFAULT_AVATAR_ID,
                previewMessage = stringResolver.get(R.string.login_avatar_default_message)
            )
        }
    }
}
