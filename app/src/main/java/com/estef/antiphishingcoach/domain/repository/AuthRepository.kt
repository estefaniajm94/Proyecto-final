package com.estef.antiphishingcoach.domain.repository

import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<AuthUser?>
    fun hasActiveSession(): Boolean
    suspend fun register(
        displayName: String,
        email: String,
        password: String,
        avatarId: String
    ): AuthActionResult
    suspend fun login(email: String, password: String): AuthActionResult
    suspend fun findUserByEmail(email: String): AuthUser?
    suspend fun updateCurrentUserAvatar(avatarId: String): Boolean
    suspend fun logout()
}
