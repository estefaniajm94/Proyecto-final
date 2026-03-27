package com.estef.antiphishingcoach.domain.model

data class CoachScenario(
    val id: String,
    val title: String,
    val summary: String,
    val threatLabel: String,
    val typicalSigns: List<String>,
    val whatToDoNow: List<String>,
    val whatToAvoid: List<String>,
    val whenToEscalate: List<String>,
    val recommendedAction: String,
    val closingNote: String
)
