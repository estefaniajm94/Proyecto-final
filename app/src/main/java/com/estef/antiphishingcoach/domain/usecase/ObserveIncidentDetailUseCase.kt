package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow

class ObserveIncidentDetailUseCase(
    private val repository: IncidentRepository
) {
    operator fun invoke(incidentId: Long): Flow<IncidentRecord?> = repository.observeIncidentDetail(incidentId)
}
