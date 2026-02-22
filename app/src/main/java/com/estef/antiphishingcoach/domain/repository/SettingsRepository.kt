package com.estef.antiphishingcoach.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeExtremePrivacy(): Flow<Boolean>
    fun observeLocalLockEnabled(): Flow<Boolean>
    suspend fun isExtremePrivacyEnabled(): Boolean
    suspend fun isLocalLockEnabled(): Boolean
    suspend fun setExtremePrivacy(enabled: Boolean)
    suspend fun setLocalLockEnabled(enabled: Boolean)
}
