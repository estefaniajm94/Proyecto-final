package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.repository.AuthRepository

class LoginLocalUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthActionResult {
        return repository.login(email, password)
    }
}
