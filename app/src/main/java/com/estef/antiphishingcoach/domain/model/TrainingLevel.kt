package com.estef.antiphishingcoach.domain.model

enum class TrainingLevel(val rawValue: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    companion object {
        fun fromRaw(rawValue: String?): TrainingLevel {
            return entries.firstOrNull { level ->
                level.rawValue.equals(rawValue?.trim(), ignoreCase = true)
            } ?: BEGINNER
        }
    }
}
