package com.estef.antiphishingcoach.presentation.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.training.QuizEngine
import com.estef.antiphishingcoach.domain.usecase.GetTrainingQuestionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainingViewModel(
    private val getTrainingQuestionsUseCase: GetTrainingQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    private var loadedQuestions: List<TrainingQuestion> = emptyList()
    private var quizEngine: QuizEngine? = null

    fun ensureLoaded() {
        if (!_uiState.value.isLoading && loadedQuestions.isNotEmpty()) return
        viewModelScope.launch {
            loadedQuestions = getTrainingQuestionsUseCase()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    totalQuestions = loadedQuestions.size
                )
            }
        }
    }

    fun startQuiz() {
        viewModelScope.launch {
            if (loadedQuestions.isEmpty()) {
                loadedQuestions = getTrainingQuestionsUseCase()
            }
            quizEngine = QuizEngine(loadedQuestions)
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
    }
}
