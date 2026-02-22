package com.estef.antiphishingcoach.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.usecase.GetCoachScenariosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScenarioChecklistViewModel(
    private val scenarioId: String,
    private val getCoachScenariosUseCase: GetCoachScenariosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScenarioChecklistUiState())
    val uiState: StateFlow<ScenarioChecklistUiState> = _uiState.asStateFlow()

    init {
        loadScenario()
    }

    private fun loadScenario() {
        viewModelScope.launch {
            val scenario = getCoachScenariosUseCase().firstOrNull { item -> item.id == scenarioId }
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    scenarioTitle = scenario?.title.orEmpty(),
                    items = scenario?.checklist.orEmpty(),
                    markedCount = 0
                )
            }
        }
    }

    fun onCheckedCountChanged(count: Int) {
        _uiState.update { state -> state.copy(markedCount = count) }
    }
}
