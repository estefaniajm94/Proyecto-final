package com.estef.antiphishingcoach.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AnalysisResultWithSignals(
    @Embedded
    val analysisResult: AnalysisResultEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "analysisResultId"
    )
    val signals: List<DetectedSignalEntity>
)

data class IncidentWithAnalysisAndSignals(
    @Embedded
    val incident: IncidentEntity,
    @Relation(
        entity = AnalysisResultEntity::class,
        parentColumn = "id",
        entityColumn = "incidentId"
    )
    val result: AnalysisResultWithSignals?
)
