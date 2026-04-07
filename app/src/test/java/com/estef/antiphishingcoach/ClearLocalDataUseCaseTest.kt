package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClearLocalDataUseCaseTest {

    @Test
    fun `invoke delega el borrado en el repositorio`() = runTest {
        var clearCalls = 0
        val repository = object : IncidentRepository {
            override suspend fun saveIncident(record: IncidentRecord): Long = 1L
            override fun observeHistory(): Flow<List<IncidentRecord>> = flowOf(emptyList())
            override fun observeLatestIncidentSummary(): Flow<IncidentSummary?> = flowOf(null)
            override fun observeIncidentDetail(incidentId: Long): Flow<IncidentRecord?> = flowOf(null)

            override suspend fun clearAll() {
                clearCalls++
            }
        }

        ClearLocalDataUseCase(repository)()

        assertEquals(1, clearCalls)
    }
}
