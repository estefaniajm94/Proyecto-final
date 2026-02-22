package com.estef.antiphishingcoach.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Resultado agregado del análisis asociado 1:1 con un incidente.
 */
@Entity(
    tableName = "analysis_results",
    foreignKeys = [
        ForeignKey(
            entity = IncidentEntity::class,
            parentColumns = ["id"],
            childColumns = ["incidentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["incidentId"], unique = true)]
)
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentId: Long,
    val createdAt: Long,
    val sanitizedDomain: String?,
    val recommendationCodes: List<String>
)
