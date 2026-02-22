package com.estef.antiphishingcoach.domain.usecase

import com.estef.antiphishingcoach.domain.heuristics.RuleEngine
import com.estef.antiphishingcoach.domain.model.AnalysisOutput

class AnalyzeInputUseCase(
    private val ruleEngine: RuleEngine = RuleEngine()
) {
    operator fun invoke(input: String): AnalysisOutput = ruleEngine.analyze(input)
}
