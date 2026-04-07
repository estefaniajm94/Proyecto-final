package com.estef.antiphishingcoach.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.training.QuizEngine
import com.estef.antiphishingcoach.domain.training.filterByLevel
import com.estef.antiphishingcoach.domain.usecase.GetTrainingQuestionsUseCase
import com.estef.antiphishingcoach.domain.usecase.SaveLatestTrainingProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainingViewModel(
    private val getTrainingQuestionsUseCase: GetTrainingQuestionsUseCase,
    private val saveLatestTrainingProgressUseCase: SaveLatestTrainingProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    private var loadedQuestions: List<TrainingQuestion> = emptyList()
    private var quizEngine: QuizEngine? = null
    private var hasPersistedCurrentResult = false

    fun ensureLoaded() {
        if (!_uiState.value.isLoading && loadedQuestions.isNotEmpty()) return
        viewModelScope.launch {
            loadQuestionsIfNeeded()
            updateQuestionAvailability()
        }
    }

    fun selectLevel(level: TrainingLevel) {
        _uiState.update { state ->
            state.copy(
                selectedLevel = level,
                selectedLevelQuestionCount = loadedQuestions.filterByLevel(level).size
            )
        }
    }

    fun startQuiz() {
        viewModelScope.launch {
            loadQuestionsIfNeeded()
            updateQuestionAvailability()
            val selectedLevel = _uiState.value.selectedLevel
            val questionsForLevel = getTrainingQuestionsUseCase(selectedLevel)
            quizEngine = QuizEngine(questionsForLevel)
            hasPersistedCurrentResult = false
            syncFromEngine()
        }
    }

    fun submitAnswer(selectedOptionIndex: Int) {
        val engine = quizEngine ?: return
        val result = engine.submitAnswer(selectedOptionIndex)
        if (!result.accepted) return
        syncFromEngine()
    }

    fun goToNextQuestion() {
        val engine = quizEngine ?: return
        if (!engine.nextQuestion()) return
        syncFromEngine()
    }

    fun restart() {
        startQuiz()
    }

    private suspend fun loadQuestionsIfNeeded() {
        if (loadedQuestions.isNotEmpty()) return
        loadedQuestions = getTrainingQuestionsUseCase()
    }

    private fun updateQuestionAvailability() {
        val counts = TrainingLevel.entries.associateWith { level ->
            loadedQuestions.filterByLevel(level).size
        }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                availableQuestionCounts = counts,
                selectedLevelQuestionCount = counts[state.selectedLevel] ?: 0
            )
        }
    }

    private fun syncFromEngine() {
        val engine = quizEngine ?: return
        val completed = engine.isCompleted()
        val question = if (completed) null else engine.currentQuestion()
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                totalQuestions = engine.totalQuestions(),
                currentPosition = if (completed) engine.totalQuestions() else engine.currentPosition(),
                currentQuestion = question,
                score = engine.score(),
                answerChecked = engine.hasAnsweredCurrent(),
                lastAnswerCorrect = engine.lastAnswerWasCorrect(),
                lastExplanation = if (engine.hasAnsweredCurrent()) engine.currentQuestion()?.explanation else null,
                completed = completed
            )
        }
        if (completed && !hasPersistedCurrentResult && engine.totalQuestions() > 0) {
            hasPersistedCurrentResult = true
            viewModelScope.launch {
                saveLatestTrainingProgressUseCase(
                    TrainingProgressSummary(
                        level = _uiState.value.selectedLevel,
                        score = engine.score(),
                        totalQuestions = engine.totalQuestions(),
                        completedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
