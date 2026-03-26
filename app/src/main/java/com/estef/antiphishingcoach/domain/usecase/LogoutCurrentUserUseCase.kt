package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.AuthRepository

class LogoutCurrentUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
