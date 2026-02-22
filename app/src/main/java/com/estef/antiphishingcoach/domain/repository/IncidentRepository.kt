package com.estef.antiphishingcoach.domain.repository

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    suspend fun saveIncident(record: IncidentRecord): Long
    fun observeHistory(): Flow<List<IncidentRecord>>
    fun observeIncidentDetail(incidentId: Long): Flow<IncidentRecord?>
    suspend fun clearAll()
}
