package com.estef.antiphishingcoach.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.model.CoachScenario
import com.estef.antiphishingcoach.domain.usecase.GetCoachScenariosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoachViewModel(
    private val getCoachScenariosUseCase: GetCoachScenariosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val scenarios = getCoachScenariosUseCase()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    scenarios = scenarios
                )
            }
        }
    }

    fun findScenarioById(scenarioId: String): CoachScenario? {
        return uiState.value.scenarios.firstOrNull { scenario -> scenario.id == scenarioId }
    }
}
