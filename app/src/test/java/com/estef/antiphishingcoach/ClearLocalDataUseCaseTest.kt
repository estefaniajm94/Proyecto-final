package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.repository.TrainingRepository
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClearLocalDataUseCaseTest {

    @Test
    fun `invoke delega el borrado en el repositorio`() = runTest {
        var clearIncidentCalls = 0
        var clearTrainingCalls = 0
        val incidentRepository = object : IncidentRepository {
            override suspend fun saveIncident(record: IncidentRecord): Long = 1L
            override fun observeHistory(): Flow<List<IncidentRecord>> = flowOf(emptyList())
            override fun observeLatestIncidentSummary(): Flow<IncidentSummary?> = flowOf(null)
            override fun observeIncidentDetail(incidentId: Long): Flow<IncidentRecord?> = flowOf(null)

            override suspend fun clearAll() {
                clearIncidentCalls++
            }
        }
        val trainingRepository = object : TrainingRepository {
            override suspend fun getQuestions(level: TrainingLevel?): List<TrainingQuestion> = emptyList()
            override fun observeLatestProgress(): Flow<TrainingProgressSummary?> = flowOf(null)
            override suspend fun saveLatestProgress(summary: TrainingProgressSummary) = Unit

            override suspend fun clearLatestProgress() {
                clearTrainingCalls++
            }
        }

        ClearLocalDataUseCase(incidentRepository, trainingRepository)()

        assertEquals(1, clearIncidentCalls)
        assertEquals(1, clearTrainingCalls)
    }
}
