package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.SettingsRepository

class IsLocalLockEnabledUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = repository.isLocalLockEnabled()
}
