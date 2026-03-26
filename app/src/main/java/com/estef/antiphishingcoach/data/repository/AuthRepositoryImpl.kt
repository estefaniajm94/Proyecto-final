package com.estef.antiphishingcoach.data.repository

import com.estef.antiphishingcoach.data.local.dao.UserDao
import com.estef.antiphishingcoach.data.local.entity.UserEntity
import com.estef.antiphishingcoach.data.local.preferences.SecureSettingsDataSource
import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.model.AuthErrorReason
import com.estef.antiphishingcoach.domain.model.AuthUser
import com.estef.antiphishingcoach.domain.repository.AuthRepository
import java.security.MessageDigest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val secureSettingsDataSource: SecureSettingsDataSource
) : AuthRepository {

    override fun observeCurrentUser(): Flow<AuthUser?> {
        return secureSettingsDataSource.observeCurrentUserId().flatMapLatest { userId ->
            if (userId == null) {
                flowOf(null)
            } else {
                userDao.observeById(userId).map { entity ->
                    entity?.toDomain()
                }
            }
        }
    }

    override fun hasActiveSession(): Boolean = secureSettingsDataSource.getCurrentUserId() != null

    override suspend fun register(
        displayName: String,
        email: String,
        password: String
    ): AuthActionResult {
        val normalizedEmail = email.normalizeEmail()
        if (userDao.findByEmail(normalizedEmail) != null) {
            return AuthActionResult.Error(AuthErrorReason.EMAIL_ALREADY_IN_USE)
        }

        val userId = userDao.insert(
            UserEntity(
                createdAt = System.currentTimeMillis(),
                displayName = displayName.trim(),
                email = normalizedEmail,
                passwordHash = hashPassword(password)
            )
        )
        secureSettingsDataSource.setCurrentUserId(userId)
        return AuthActionResult.Success(
            AuthUser(
                id = userId,
                displayName = displayName.trim(),
                email = normalizedEmail
            )
        )
    }

    override suspend fun login(email: String, password: String): AuthActionResult {
        val normalizedEmail = email.normalizeEmail()
        val user = userDao.findByEmail(normalizedEmail)
            ?: return AuthActionResult.Error(AuthErrorReason.INVALID_CREDENTIALS)

        if (user.passwordHash != hashPassword(password)) {
            return AuthActionResult.Error(AuthErrorReason.INVALID_CREDENTIALS)
        }

        secureSettingsDataSource.setCurrentUserId(user.id)
        return AuthActionResult.Success(user.toDomain())
    }

    override suspend fun logout() {
        secureSettingsDataSource.setCurrentUserId(null)
    }

    private fun UserEntity.toDomain(): AuthUser {
        return AuthUser(
            id = id,
            displayName = displayName,
            email = email
        )
    }

    private fun String.normalizeEmail(): String = trim().lowercase()

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }
}
