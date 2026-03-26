package com.estef.antiphishingcoach.presentation.auth

import com.estef.antiphishingcoach.domain.model.AuthUser

data class AuthGateUiState(
    val isLoading: Boolean = true,
    val currentUser: AuthUser? = null
)
