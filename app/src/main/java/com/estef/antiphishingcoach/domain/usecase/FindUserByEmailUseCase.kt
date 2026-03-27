package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.AuthUser
import com.estef.antiphishingcoach.domain.repository.AuthRepository

class FindUserByEmailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): AuthUser? {
        return repository.findUserByEmail(email)
    }
}
