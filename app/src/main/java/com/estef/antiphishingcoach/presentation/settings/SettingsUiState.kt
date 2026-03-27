package com.estef.antiphishingcoach.presentation.settings

data class SettingsUiState(
    val extremePrivacyEnabled: Boolean = false,
    val localLockEnabled: Boolean = false,
    val currentUserName: String? = null,
    val currentUserEmail: String? = null,
    val currentUserAvatarId: String? = null,
    val statusMessage: String? = null,
    val logoutCompleted: Boolean = false
)
