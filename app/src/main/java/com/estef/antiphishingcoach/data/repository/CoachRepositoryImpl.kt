package com.estef.antiphishingcoach.data.repository

import com.estef.antiphishingcoach.data.local.seed.SeedAssetLoader
import com.estef.antiphishingcoach.domain.model.CoachScenario
import com.estef.antiphishingcoach.domain.repository.CoachRepository

class CoachRepositoryImpl(
    private val seedLoader: SeedAssetLoader
) : CoachRepository {
    override suspend fun getScenarios(): List<CoachScenario> {
        return seedLoader.loadCoachScenarios().map { dto ->
            CoachScenario(
                id = dto.id,
                title = dto.title,
                checklist = dto.checklist
            )
        }
    }
}
