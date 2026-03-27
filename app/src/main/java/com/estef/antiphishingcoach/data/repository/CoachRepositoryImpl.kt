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
                summary = dto.summary.cleanText(),
                threatLabel = dto.threatLabel.cleanText(),
                typicalSigns = dto.typicalSigns.cleanList(),
                whatToDoNow = (dto.whatToDoNow ?: dto.checklist).cleanList(),
                whatToAvoid = dto.whatToAvoid.cleanList(),
                whenToEscalate = dto.whenToEscalate.cleanList(),
                recommendedAction = dto.recommendedAction.cleanText(),
                closingNote = dto.closingNote.cleanText()
            )
        }
    }

    private fun String?.cleanText(): String {
        return this?.trim().orEmpty()
    }

    private fun List<String>?.cleanList(): List<String> {
        return this.orEmpty().map(String::trim).filter(String::isNotBlank)
    }
}
