package com.estef.antiphishingcoach.data.local.seed

data class CoachScenarioDto(
    val id: String,
    val title: String,
    val summary: String? = null,
    val threatLabel: String? = null,
    val typicalSigns: List<String>? = null,
    val whatToDoNow: List<String>? = null,
    val checklist: List<String>? = null,
    val whatToAvoid: List<String>? = null,
    val whenToEscalate: List<String>? = null,
    val recommendedAction: String? = null,
    val closingNote: String? = null
)
