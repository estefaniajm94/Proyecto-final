package com.estef.antiphishingcoach.data.repository

import com.estef.antiphishingcoach.data.local.preferences.SecureSettingsDataSource
import com.estef.antiphishingcoach.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dataSource: SecureSettingsDataSource
) : SettingsRepository {
    override fun observeExtremePrivacy(): Flow<Boolean> = dataSource.observeExtremePrivacy()
    override fun observeLocalLockEnabled(): Flow<Boolean> = dataSource.observeLocalLock()

    override suspend fun isExtremePrivacyEnabled(): Boolean = dataSource.isExtremePrivacyEnabled()
    override suspend fun isLocalLockEnabled(): Boolean = dataSource.isLocalLockEnabled()

    override suspend fun setExtremePrivacy(enabled: Boolean) {
        dataSource.setExtremePrivacy(enabled)
    }

    override suspend fun setLocalLockEnabled(enabled: Boolean) {
        dataSource.setLocalLockEnabled(enabled)
    }
}
