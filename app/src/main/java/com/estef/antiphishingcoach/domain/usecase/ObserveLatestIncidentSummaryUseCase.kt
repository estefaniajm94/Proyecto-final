package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow

class ObserveLatestIncidentSummaryUseCase(
    private val repository: IncidentRepository
) {
    operator fun invoke(): Flow<IncidentSummary?> = repository.observeLatestIncidentSummary()
}
