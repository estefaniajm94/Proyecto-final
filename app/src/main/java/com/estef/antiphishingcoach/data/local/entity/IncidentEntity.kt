package com.estef.antiphishingcoach.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Metadatos del incidente analizado (sin texto original).
 */
@Entity(
    tableName = "incidents",
    indices = [Index(value = ["createdAt"])]
)
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long,
    val title: String?,
    val sourceType: String,
    val sourceApp: String,
    val scenarioType: String?,
    val trafficLight: String,
    val score: Int
)
