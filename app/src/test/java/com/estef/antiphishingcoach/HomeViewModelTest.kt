package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.AuthUser
import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.model.TrainingLevel
import com.estef.antiphishingcoach.domain.model.TrainingProgressSummary
import com.estef.antiphishingcoach.domain.model.TrainingQuestion
import com.estef.antiphishingcoach.domain.repository.AuthRepository
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.repository.TrainingRepository
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestIncidentSummaryUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLatestTrainingProgressUseCase
import com.estef.antiphishingcoach.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val currentUserFlow = MutableStateFlow<AuthUser?>(null)
    private val latestTrainingFlow = MutableStateFlow<TrainingProgressSummary?>(null)

    private val authRepository = object : AuthRepository {
        override fun observeCurrentUser(): Flow<AuthUser?> = currentUserFlow
        override fun hasActiveSession(): Boolean = false
        override suspend fun register(
            displayName: String,
            email: String,
            password: String,
            avatarId: String
        ) = throw NotImplementedError()

        override suspend fun login(email: String, password: String) =
            throw NotImplementedError()

        override suspend fun findUserByEmail(email: String): AuthUser? = null
        override suspend fun updateCurrentUserAvatar(avatarId: String): Boolean = true
        override suspend fun logout() = Unit
    }

    private val incidentRepository = object : IncidentRepository {
        override suspend fun saveIncident(record: com.estef.antiphishingcoach.domain.model.IncidentRecord): Long = 1L
        override fun observeHistory(): Flow<List<com.estef.antiphishingcoach.domain.model.IncidentRecord>> = flowOf(emptyList())
        override fun observeLatestIncidentSummary(): Flow<IncidentSummary?> = flowOf(null)
        override fun observeIncidentDetail(incidentId: Long): Flow<com.estef.antiphishingcoach.domain.model.IncidentRecord?> = flowOf(null)
        override suspend fun clearAll() = Unit
    }

    private val trainingRepository = object : TrainingRepository {
        override suspend fun getQuestions(level: TrainingLevel?): List<TrainingQuestion> = emptyList()
        override fun observeLatestProgress(): Flow<TrainingProgressSummary?> = latestTrainingFlow
        override suspend fun saveLatestProgress(summary: TrainingProgressSummary) = Unit
        override suspend fun clearLatestProgress() = Unit
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
    fun `cuando llega un ultimo entrenamiento home muestra su resumen`() = runTest {
        val viewModel = HomeViewModel(
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(authRepository),
            observeLatestIncidentSummaryUseCase = ObserveLatestIncidentSummaryUseCase(incidentRepository),
            observeLatestTrainingProgressUseCase = ObserveLatestTrainingProgressUseCase(trainingRepository),
            stringResolver = TestStringResolver()
        )
        advanceUntilIdle()

        latestTrainingFlow.value = TrainingProgressSummary(
            level = TrainingLevel.INTERMEDIATE,
            score = 3,
            totalQuestions = 5,
            completedAt = 1_700_000_000_000L
        )
        advanceUntilIdle()

        val summary = viewModel.uiState.value.latestTrainingSummary
        assertTrue(summary.contains("Intermedio"))
        assertTrue(summary.contains("Aciertos: 3/5"))
        assertTrue(summary.contains("Fecha:"))
    }
}
