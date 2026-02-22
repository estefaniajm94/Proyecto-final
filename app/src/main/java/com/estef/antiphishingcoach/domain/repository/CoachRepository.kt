package com.estef.antiphishingcoach.domain.repository

import com.estef.antiphishingcoach.domain.model.CoachScenario

interface CoachRepository {
    suspend fun getScenarios(): List<CoachScenario>
}
