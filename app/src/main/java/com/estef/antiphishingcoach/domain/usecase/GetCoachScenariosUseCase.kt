package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.model.CoachScenario
import com.estef.antiphishingcoach.domain.repository.CoachRepository

class GetCoachScenariosUseCase(
    private val repository: CoachRepository
) {
    suspend operator fun invoke(): List<CoachScenario> = repository.getScenarios()
}
