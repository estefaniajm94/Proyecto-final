package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.SettingsRepository

class ToggleLocalLockUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setLocalLockEnabled(enabled)
    }
}
