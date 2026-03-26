package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.AuthUser
import com.estef.antiphishingcoach.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> = repository.observeCurrentUser()
}
