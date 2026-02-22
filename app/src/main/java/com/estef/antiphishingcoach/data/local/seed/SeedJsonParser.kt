package com.estef.antiphishingcoach.data.local.seed

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parser puro de JSON seed para poder testear sin dependencia de Android Context.
 */
class SeedJsonParser(
    private val gson: Gson = Gson()
) {
    fun parseCoachScenarios(json: String): List<CoachScenarioDto> {
        val type = object : TypeToken<List<CoachScenarioDto>>() {}.type
        val parsed: List<CoachScenarioDto> = gson.fromJson(json, type) ?: emptyList()
        return parsed.filter { dto ->
            dto.id.isNotBlank() && dto.title.isNotBlank() && dto.checklist.isNotEmpty()
        }
    }

    fun parseTrainingQuestions(json: String): List<TrainingQuestionDto> {
        val type = object : TypeToken<List<TrainingQuestionDto>>() {}.type
        val parsed: List<TrainingQuestionDto> = gson.fromJson(json, type) ?: emptyList()
        return parsed.filter { dto ->
            dto.id.isNotBlank() &&
                dto.prompt.isNotBlank() &&
                dto.options.size >= 2 &&
                dto.correctIndex in dto.options.indices
        }
    }
}
