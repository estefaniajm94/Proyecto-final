package com.estef.antiphishingcoach.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.model.AuthErrorReason
import com.estef.antiphishingcoach.domain.usecase.RegisterLocalUserUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerLocalUserUseCase: RegisterLocalUserUseCase,
    private val stringResolver: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val normalizedName = displayName.trim()
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()
        val normalizedConfirmPassword = confirmPassword.trim()

        val nameError = if (normalizedName.isBlank()) {
            stringResolver.get(R.string.auth_error_name_required)
        } else {
            null
        }
        val emailError = when {
            normalizedEmail.isBlank() -> stringResolver.get(R.string.auth_error_email_required)
            !EMAIL_REGEX.matches(normalizedEmail) -> stringResolver.get(R.string.auth_error_email_invalid)
            else -> null
        }
        val passwordError = when {
            normalizedPassword.isBlank() -> stringResolver.get(R.string.auth_error_password_required)
            normalizedPassword.length < 6 -> stringResolver.get(R.string.auth_error_password_length)
            else -> null
        }
        val confirmPasswordError = when {
            normalizedConfirmPassword.isBlank() -> stringResolver.get(R.string.auth_error_confirm_password_required)
            normalizedConfirmPassword != normalizedPassword -> stringResolver.get(R.string.auth_error_password_mismatch)
            else -> null
        }

        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.update { state ->
                state.copy(
                    displayNameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
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
                    displayNameError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    statusMessage = null,
                    authenticatedUser = null
                )
            }

            when (val result = registerLocalUserUseCase(normalizedName, normalizedEmail, normalizedPassword)) {
                is AuthActionResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            authenticatedUser = result.user
                        )
                    }
                }

                is AuthActionResult.Error -> {
                    val message = when (result.reason) {
                        AuthErrorReason.EMAIL_ALREADY_IN_USE -> stringResolver.get(R.string.register_error_email_in_use)
                        AuthErrorReason.INVALID_CREDENTIALS -> stringResolver.get(R.string.auth_error_generic)
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

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}
