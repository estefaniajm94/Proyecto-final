package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.SettingsRepository

class ToggleExtremePrivacyUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setExtremePrivacy(enabled)
    }
}
