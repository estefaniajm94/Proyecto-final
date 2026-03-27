package com.estef.antiphishingcoach.presentation.auth

import com.estef.antiphishingcoach.domain.model.AuthUser

data class LoginUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val statusMessage: String? = null,
    val authenticatedUser: AuthUser? = null,
    val previewAvatarId: String? = null,
    val previewMessage: String? = null
)
