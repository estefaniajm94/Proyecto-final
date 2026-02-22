package com.estef.antiphishingcoach.data.local.seed

import android.content.Context
import com.google.gson.Gson

/**
 * Carga datos locales precargados desde assets.
 */
class SeedAssetLoader(
    private val context: Context,
    gson: Gson = Gson()
) {
    private val parser = SeedJsonParser(gson)

    fun loadCoachScenarios(): List<CoachScenarioDto> {
        val json = readAsset("coach_scenarios.json")
        return parser.parseCoachScenarios(json)
    }

    fun loadTrainingQuestions(): List<TrainingQuestionDto> {
        val json = readAsset("training_questions.json")
        return parser.parseTrainingQuestions(json)
    }

    private fun readAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
