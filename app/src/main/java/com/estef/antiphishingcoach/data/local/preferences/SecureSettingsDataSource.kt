package com.estef.antiphishingcoach.data.local.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ajustes locales cifrados para banderas sensibles del MVP.
 */
class SecureSettingsDataSource(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        PREF_FILE,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val privacyFlow = MutableStateFlow(isExtremePrivacyEnabled())
    private val localLockFlow = MutableStateFlow(isLocalLockEnabled())
    private val currentUserIdFlow = MutableStateFlow(getCurrentUserId())

    fun observeExtremePrivacy(): Flow<Boolean> = privacyFlow.asStateFlow()
    fun observeLocalLock(): Flow<Boolean> = localLockFlow.asStateFlow()
    fun observeCurrentUserId(): Flow<Long?> = currentUserIdFlow.asStateFlow()

    fun isExtremePrivacyEnabled(): Boolean = prefs.getBoolean(KEY_EXTREME_PRIVACY, false)
    fun isLocalLockEnabled(): Boolean = prefs.getBoolean(KEY_LOCAL_LOCK_ENABLED, false)
    fun getCurrentUserId(): Long? {
        if (!prefs.contains(KEY_CURRENT_USER_ID)) return null
        return prefs.getLong(KEY_CURRENT_USER_ID, -1L).takeIf { it > 0L }
    }

    fun setExtremePrivacy(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EXTREME_PRIVACY, enabled).apply()
        privacyFlow.value = enabled
    }

    fun setLocalLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCAL_LOCK_ENABLED, enabled).apply()
        localLockFlow.value = enabled
    }

    fun setCurrentUserId(userId: Long?) {
        prefs.edit().apply {
            if (userId == null) {
                remove(KEY_CURRENT_USER_ID)
            } else {
                putLong(KEY_CURRENT_USER_ID, userId)
            }
        }.apply()
        currentUserIdFlow.value = userId
    }

    companion object {
        private const val PREF_FILE = "secure_settings"
        private const val KEY_EXTREME_PRIVACY = "extreme_privacy"
        private const val KEY_LOCAL_LOCK_ENABLED = "local_lock_enabled"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}
