package com.estef.antiphishingcoach.data.repository

import com.estef.antiphishingcoach.data.local.entity.AnalysisResultWithSignals
import com.estef.antiphishingcoach.data.local.entity.DetectedSignalEntity
import com.estef.antiphishingcoach.data.local.entity.IncidentWithAnalysisAndSignals
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.IncidentSummary

internal fun IncidentWithAnalysisAndSignals.toDomain(): IncidentRecord {
    val localResult: AnalysisResultWithSignals? = result
    return IncidentRecord(
        id = incident.id,
        createdAt = incident.createdAt,
        title = incident.title,
        sourceType = incident.sourceType,
        sourceApp = incident.sourceApp,
        scenarioType = incident.scenarioType,
        trafficLight = incident.trafficLight,
        score = incident.score,
        sanitizedDomain = localResult?.analysisResult?.sanitizedDomain,
        recommendationCodes = localResult?.analysisResult?.recommendationCodes.orEmpty(),
        signals = localResult?.signals.orEmpty().map { it.toDomain() }
    )
}

internal fun DetectedSignalEntity.toDomain(): DetectedSignal {
    return DetectedSignal(
        signalCode = signalCode,
        title = title,
        explanation = explanation,
        weight = weight
    )
}

internal fun IncidentWithAnalysisAndSignals.toSummary(): IncidentSummary {
    return IncidentSummary(
        incidentId = incident.id,
        createdAt = incident.createdAt,
        title = incident.title,
        sourceApp = incident.sourceApp,
        trafficLight = incident.trafficLight,
        score = incident.score,
        sanitizedDomain = result?.analysisResult?.sanitizedDomain
    )
}
