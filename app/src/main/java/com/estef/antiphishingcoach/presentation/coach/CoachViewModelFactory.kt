package com.estef.antiphishingcoach.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.estef.antiphishingcoach.domain.usecase.GetCoachScenariosUseCase

class CoachViewModelFactory(
    private val getCoachScenariosUseCase: GetCoachScenariosUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoachViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoachViewModel(getCoachScenariosUseCase) as T
        }
        throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}
