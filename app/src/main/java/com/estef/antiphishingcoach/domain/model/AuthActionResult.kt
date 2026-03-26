package com.estef.antiphishingcoach.domain.model

sealed interface AuthActionResult {
    data class Success(val user: AuthUser) : AuthActionResult
    data class Error(val reason: AuthErrorReason) : AuthActionResult
}

enum class AuthErrorReason {
    EMAIL_ALREADY_IN_USE,
    INVALID_CREDENTIALS
}
