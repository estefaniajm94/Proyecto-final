package com.estef.antiphishingcoach.presentation.settings

data class SettingsUiState(
    val extremePrivacyEnabled: Boolean = false,
    val localLockEnabled: Boolean = false,
    val statusMessage: String? = null
)
