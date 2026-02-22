package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveExtremePrivacyUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeExtremePrivacy()
}
