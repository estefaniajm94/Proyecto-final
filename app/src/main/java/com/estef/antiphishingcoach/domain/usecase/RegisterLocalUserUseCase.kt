package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.repository.AuthRepository

class RegisterLocalUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        displayName: String,
        email: String,
        password: String,
        avatarId: String
    ): AuthActionResult {
        return repository.register(displayName, email, password, avatarId)
    }
}
