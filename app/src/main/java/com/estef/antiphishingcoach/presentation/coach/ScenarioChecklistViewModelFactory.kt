package com.estef.antiphishingcoach.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.GetCoachScenariosUseCase

class ScenarioChecklistViewModelFactory(
    private val scenarioId: String,
    private val getCoachScenariosUseCase: GetCoachScenariosUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScenarioChecklistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScenarioChecklistViewModel(
                scenarioId = scenarioId,
                getCoachScenariosUseCase = getCoachScenariosUseCase
            ) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
