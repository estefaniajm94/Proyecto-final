package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.AuthRepository

class UpdateCurrentUserAvatarUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(avatarId: String): Boolean {
        return repository.updateCurrentUserAvatar(avatarId)
    }
}
