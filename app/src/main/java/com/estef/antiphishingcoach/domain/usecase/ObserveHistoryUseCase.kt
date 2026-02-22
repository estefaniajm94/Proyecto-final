package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(
    private val repository: IncidentRepository
) {
    operator fun invoke(): Flow<List<IncidentRecord>> = repository.observeHistory()
}
