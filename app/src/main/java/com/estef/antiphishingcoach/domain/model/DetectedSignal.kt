package com.estef.antiphishingcoach.domain.model

data class DetectedSignal(
    val signalCode: String,
    val title: String,
    val explanation: String,
    val weight: Int
)
