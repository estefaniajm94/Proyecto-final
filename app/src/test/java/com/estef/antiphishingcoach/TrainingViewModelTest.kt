package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.repository.TrainingRepository
import com.estef.antiphishingcoach.domain.usecase.GetTrainingQuestionsUseCase
import com.estef.antiphishingcoach.domain.usecase.SaveLatestTrainingProgressUseCase
import com.estef.antiphishingcoach.presentation.training.TrainingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val latestProgressFlow = MutableStateFlow<TrainingProgressSummary?>(null)
    private var savedProgress: TrainingProgressSummary? = null

    private val trainingRepository = object : TrainingRepository {
        override suspend fun getQuestions(level: TrainingLevel?): List<TrainingQuestion> {
            return listOf(
                TrainingQuestion(
                    id = "q1",
                    prompt = "Pregunta de prueba",
                    options = listOf("Correcta", "Incorrecta"),
                    correctIndex = 0,
                    explanation = "Explicacion",
                    level = TrainingLevel.BEGINNER,
                    category = "General"
                )
            )
        }

        override fun observeLatestProgress(): Flow<TrainingProgressSummary?> = latestProgressFlow

        override suspend fun saveLatestProgress(summary: TrainingProgressSummary) {
            savedProgress = summary
            latestProgressFlow.value = summary
        }

        override suspend fun clearLatestProgress() {
            latestProgressFlow.value = null
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `completar quiz guarda el ultimo entrenamiento`() = runTest {
        val viewModel = TrainingViewModel(
            getTrainingQuestionsUseCase = GetTrainingQuestionsUseCase(trainingRepository),
            saveLatestTrainingProgressUseCase = SaveLatestTrainingProgressUseCase(trainingRepository)
        )

        viewModel.startQuiz()
        advanceUntilIdle()

        viewModel.submitAnswer(0)
        advanceUntilIdle()
        viewModel.goToNextQuestion()
        advanceUntilIdle()

        val saved = savedProgress
        assertNotNull(saved)
        assertEquals(TrainingLevel.BEGINNER, saved?.level)
        assertEquals(1, saved?.score)
        assertEquals(1, saved?.totalQuestions)
    }
}
