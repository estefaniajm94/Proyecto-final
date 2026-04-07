package com.estef.antiphishingcoach.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestIncidentSummaryUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestTrainingProgressUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver
import com.estef.antiphishingcoach.presentation.training.labelResId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    observeLatestIncidentSummaryUseCase: ObserveLatestIncidentSummaryUseCase,
    observeLatestTrainingProgressUseCase: ObserveLatestTrainingProgressUseCase,
    private val stringResolver: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            latestTrainingSummary = stringResolver.get(R.string.home_training_no_progress)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update { state ->
                    state.copy(
                        currentUserName = user?.displayName,
                        currentUserAvatarId = user?.avatarId
                    )
                }
            }
        }
        viewModelScope.launch {
            observeLatestIncidentSummaryUseCase().collect { latest ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        latestIncident = latest?.toUi()
                    )
                }
            }
        }
        viewModelScope.launch {
            observeLatestTrainingProgressUseCase().collect { latest ->
                _uiState.update { state ->
                    state.copy(
                        latestTrainingSummary = latest?.toUiSummary()
                            ?: stringResolver.get(R.string.home_training_no_progress)
                    )
                }
            }
        }
    }

    private fun IncidentSummary.toUi(): LatestIncidentUi {
        return LatestIncidentUi(
            incidentId = incidentId,
            title = title ?: stringResolver.get(R.string.history_item_title_fallback),
            createdAtLine = stringResolver.get(R.string.home_latest_date, dateFormat.format(Date(createdAt))),
            score = score,
            trafficLightCode = trafficLight,
            sanitizedDomainLine = sanitizedDomain?.let { domain ->
                stringResolver.get(R.string.home_latest_domain, domain)
            }
        )
    }

    private fun TrainingProgressSummary.toUiSummary(): String {
        return stringResolver.get(
            R.string.home_latest_training_summary,
            stringResolver.get(level.labelResId()),
            stringResolver.get(R.string.training_result_score, score, totalQuestions),
            stringResolver.get(R.string.home_latest_date, dateFormat.format(Date(completedAt)))
        )
    }
}
