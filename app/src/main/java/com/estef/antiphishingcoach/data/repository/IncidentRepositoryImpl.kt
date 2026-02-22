package com.estef.antiphishingcoach.data.repository

import androidx.room.withTransaction
import com.estef.antiphishingcoach.data.local.dao.AnalysisResultDao
import com.estef.antiphishingcoach.data.local.dao.DetectedSignalDao
import com.estef.antiphishingcoach.data.local.dao.IncidentDao
import com.estef.antiphishingcoach.data.local.db.AppDatabase
import com.estef.antiphishingcoach.data.local.entity.AnalysisResultEntity
import com.estef.antiphishingcoach.data.local.entity.DetectedSignalEntity
import com.estef.antiphishingcoach.data.local.entity.IncidentEntity
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IncidentRepositoryImpl(
    private val database: AppDatabase,
    private val incidentDao: IncidentDao,
    private val analysisResultDao: AnalysisResultDao,
    private val detectedSignalDao: DetectedSignalDao
) : IncidentRepository {

    override suspend fun saveIncident(record: IncidentRecord): Long {
        return database.withTransaction {
            val incidentId = incidentDao.insert(
                IncidentEntity(
                    createdAt = record.createdAt,
                    title = record.title,
                    sourceType = record.sourceType,
                    sourceApp = record.sourceApp,
                    scenarioType = record.scenarioType,
                    trafficLight = record.trafficLight,
                    score = record.score
                )
            )
            val analysisId = analysisResultDao.insert(
                AnalysisResultEntity(
                    incidentId = incidentId,
                    createdAt = record.createdAt,
                    sanitizedDomain = record.sanitizedDomain,
                    recommendationCodes = record.recommendationCodes
                )
            )
            val signals = record.signals.map { signal ->
                DetectedSignalEntity(
                    analysisResultId = analysisId,
                    signalCode = signal.signalCode,
                    title = signal.title,
                    explanation = signal.explanation,
                    weight = signal.weight
                )
            }
            if (signals.isNotEmpty()) {
                detectedSignalDao.insertAll(signals)
            }
            incidentId
        }
    }

    override fun observeHistory(): Flow<List<IncidentRecord>> {
        return incidentDao.observeHistory().map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeIncidentDetail(incidentId: Long): Flow<IncidentRecord?> {
        return incidentDao.observeIncidentDetail(incidentId).map { row -> row?.toDomain() }
    }

    override suspend fun clearAll() {
        incidentDao.clearAll()
    }
}
