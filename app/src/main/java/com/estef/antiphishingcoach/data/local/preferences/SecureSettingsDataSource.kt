package com.estef.antiphishingcoach.data.local.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
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
    private val latestTrainingProgressFlow = MutableStateFlow(getLatestTrainingProgress())

    fun observeExtremePrivacy(): Flow<Boolean> = privacyFlow.asStateFlow()
    fun observeLocalLock(): Flow<Boolean> = localLockFlow.asStateFlow()
    fun observeCurrentUserId(): Flow<Long?> = currentUserIdFlow.asStateFlow()
    fun observeLatestTrainingProgress(): Flow<TrainingProgressSummary?> =
        latestTrainingProgressFlow.asStateFlow()

    fun isExtremePrivacyEnabled(): Boolean = prefs.getBoolean(KEY_EXTREME_PRIVACY, false)
    fun isLocalLockEnabled(): Boolean = prefs.getBoolean(KEY_LOCAL_LOCK_ENABLED, false)
    fun getCurrentUserId(): Long? {
        if (!prefs.contains(KEY_CURRENT_USER_ID)) return null
        return prefs.getLong(KEY_CURRENT_USER_ID, -1L).takeIf { it > 0L }
    }
    fun getLatestTrainingProgress(): TrainingProgressSummary? {
        val rawLevel = prefs.getString(KEY_LATEST_TRAINING_LEVEL, null) ?: return null
        if (!prefs.contains(KEY_LATEST_TRAINING_SCORE) ||
            !prefs.contains(KEY_LATEST_TRAINING_TOTAL) ||
            !prefs.contains(KEY_LATEST_TRAINING_COMPLETED_AT)
        ) {
            return null
        }

        val score = prefs.getInt(KEY_LATEST_TRAINING_SCORE, -1)
        val totalQuestions = prefs.getInt(KEY_LATEST_TRAINING_TOTAL, -1)
        val completedAt = prefs.getLong(KEY_LATEST_TRAINING_COMPLETED_AT, -1L)
        if (score < 0 || totalQuestions <= 0 || completedAt <= 0L) return null

        return TrainingProgressSummary(
            level = TrainingLevel.fromRaw(rawLevel),
            score = score,
            totalQuestions = totalQuestions,
            completedAt = completedAt
        )
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

    fun setLatestTrainingProgress(summary: TrainingProgressSummary?) {
        prefs.edit().apply {
            if (summary == null) {
                remove(KEY_LATEST_TRAINING_LEVEL)
                remove(KEY_LATEST_TRAINING_SCORE)
                remove(KEY_LATEST_TRAINING_TOTAL)
                remove(KEY_LATEST_TRAINING_COMPLETED_AT)
            } else {
                putString(KEY_LATEST_TRAINING_LEVEL, summary.level.rawValue)
                putInt(KEY_LATEST_TRAINING_SCORE, summary.score)
                putInt(KEY_LATEST_TRAINING_TOTAL, summary.totalQuestions)
                putLong(KEY_LATEST_TRAINING_COMPLETED_AT, summary.completedAt)
            }
        }.apply()
        latestTrainingProgressFlow.value = summary
    }

    companion object {
        private const val PREF_FILE = "secure_settings"
        private const val KEY_EXTREME_PRIVACY = "extreme_privacy"
        private const val KEY_LOCAL_LOCK_ENABLED = "local_lock_enabled"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_LATEST_TRAINING_LEVEL = "latest_training_level"
        private const val KEY_LATEST_TRAINING_SCORE = "latest_training_score"
        private const val KEY_LATEST_TRAINING_TOTAL = "latest_training_total"
        private const val KEY_LATEST_TRAINING_COMPLETED_AT = "latest_training_completed_at"
    }
}
