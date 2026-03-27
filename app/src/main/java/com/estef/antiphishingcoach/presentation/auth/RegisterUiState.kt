package com.estef.antiphishingcoach.presentation.auth

import com.estef.antiphishingcoach.domain.model.AuthUser

data class RegisterUiState(
    val isLoading: Boolean = false,
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val statusMessage: String? = null,
    val authenticatedUser: AuthUser? = null,
    val selectedAvatarId: String? = null
)
