package com.estef.antiphishingcoach.presentation.training

import androidx.annotation.StringRes
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.domain.model.TrainingLevel

@StringRes
fun TrainingLevel.labelResId(): Int = when (this) {
    TrainingLevel.BEGINNER -> R.string.training_level_beginner
    TrainingLevel.INTERMEDIATE -> R.string.training_level_intermediate
    TrainingLevel.ADVANCED -> R.string.training_level_advanced
}

@StringRes
fun TrainingLevel.descriptionResId(): Int = when (this) {
    TrainingLevel.BEGINNER -> R.string.training_level_beginner_description
    TrainingLevel.INTERMEDIATE -> R.string.training_level_intermediate_description
    TrainingLevel.ADVANCED -> R.string.training_level_advanced_description
}

@StringRes
fun TrainingLevel.resultMessageResId(): Int = when (this) {
    TrainingLevel.BEGINNER -> R.string.training_result_message_beginner
    TrainingLevel.INTERMEDIATE -> R.string.training_result_message_intermediate
    TrainingLevel.ADVANCED -> R.string.training_result_message_advanced
}
