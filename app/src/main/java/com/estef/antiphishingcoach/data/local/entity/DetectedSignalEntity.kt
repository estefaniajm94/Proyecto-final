package com.estef.antiphishingcoach.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Señales explicables detectadas por regla.
 */
@Entity(
    tableName = "detected_signals",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["analysisResultId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["analysisResultId"])]
)
data class DetectedSignalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val analysisResultId: Long,
    val signalCode: String,
    val title: String,
    val explanation: String,
    val weight: Int
)
