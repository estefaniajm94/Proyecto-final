package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.IncidentSummary
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.repository.SettingsRepository
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveHistoryUseCase
import com.estef.antiphishingcoach.presentation.history.HistorySortMode
import com.estef.antiphishingcoach.presentation.history.HistoryTrafficLightFilter
import com.estef.antiphishingcoach.presentation.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val historyFlow = MutableStateFlow<List<IncidentRecord>>(emptyList())
    private val extremePrivacyFlow = MutableStateFlow(false)

    private val incidentRepository = object : IncidentRepository {
        override suspend fun saveIncident(record: IncidentRecord): Long = 1L
        override fun observeHistory(): Flow<List<IncidentRecord>> = historyFlow
        override fun observeLatestIncidentSummary(): Flow<IncidentSummary?> {
            return historyFlow.map { records ->
                records.firstOrNull()?.let { record ->
                    IncidentSummary(
                        incidentId = record.id,
                        createdAt = record.createdAt,
                        title = record.title,
                        sourceApp = record.sourceApp,
                        trafficLight = record.trafficLight,
                        score = record.score
                    )
                }
            }
        }

        override fun observeIncidentDetail(incidentId: Long): Flow<IncidentRecord?> = flowOf(null)
        override suspend fun clearAll() = Unit
    }

    private val settingsRepository = object : SettingsRepository {
        override fun observeExtremePrivacy(): Flow<Boolean> = extremePrivacyFlow
        override fun observeLocalLockEnabled(): Flow<Boolean> = flowOf(false)
        override suspend fun isExtremePrivacyEnabled(): Boolean = extremePrivacyFlow.value
        override suspend fun isLocalLockEnabled(): Boolean = false
        override suspend fun setExtremePrivacy(enabled: Boolean) {
            extremePrivacyFlow.value = enabled
        }

        override suspend fun setLocalLockEnabled(enabled: Boolean) = Unit
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
    fun `filtra por query en titulo y dominio`() = runTest {
        val viewModel = buildViewModel()
        historyFlow.value = listOf(
            record(id = 1L, title = "SMS Correos", domain = "correos.es", light = "YELLOW", score = 48),
            record(id = 2L, title = "Banco", domain = "banco.es", light = "GREEN", score = 12)
        )
        advanceUntilIdle()

        viewModel.onQueryChanged("correo")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(1L, viewModel.uiState.value.items.first().incidentId)
    }

    @Test
    fun `filtra por semaforo seleccionado`() = runTest {
        val viewModel = buildViewModel()
        historyFlow.value = listOf(
            record(id = 1L, title = "Caso verde", domain = null, light = "GREEN", score = 8),
            record(id = 2L, title = "Caso rojo", domain = null, light = "RED", score = 90)
        )
        advanceUntilIdle()

        viewModel.onTrafficLightFilterChanged(HistoryTrafficLightFilter.RED)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("RED", viewModel.uiState.value.items.first().trafficLight)
    }

    @Test
    fun `ordena por mayor riesgo cuando se selecciona sort correspondiente`() = runTest {
        val viewModel = buildViewModel()
        historyFlow.value = listOf(
            record(id = 1L, title = "medio", domain = null, light = "YELLOW", score = 45, createdAt = 1000L),
            record(id = 2L, title = "alto", domain = null, light = "RED", score = 92, createdAt = 900L),
            record(id = 3L, title = "bajo", domain = null, light = "GREEN", score = 12, createdAt = 1100L)
        )
        advanceUntilIdle()

        viewModel.onSortModeChanged(HistorySortMode.HIGHEST_RISK)
        advanceUntilIdle()

        val orderedIds = viewModel.uiState.value.items.map { it.incidentId }
        assertEquals(listOf(2L, 1L, 3L), orderedIds)
    }

    @Test
    fun `muestra mensaje de privacidad extrema en vacio`() = runTest {
        val viewModel = buildViewModel()
        historyFlow.value = emptyList()
        extremePrivacyFlow.value = true
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.emptyMessage.contains("Privacidad extrema"))
    }

    private fun buildViewModel(): HistoryViewModel {
        return HistoryViewModel(
            observeHistoryUseCase = ObserveHistoryUseCase(incidentRepository),
            observeExtremePrivacyUseCase = ObserveExtremePrivacyUseCase(settingsRepository)
        )
    }

    private fun record(
        id: Long,
        title: String,
        domain: String?,
        light: String,
        score: Int,
        createdAt: Long = 1_000_000_000L
    ): IncidentRecord {
        return IncidentRecord(
            id = id,
            createdAt = createdAt,
            title = title,
            sourceType = "TEXT",
            sourceApp = "SMS",
            scenarioType = null,
            trafficLight = light,
            score = score,
            sanitizedDomain = domain,
            recommendationCodes = emptyList(),
            signals = listOf(
                DetectedSignal(
                    signalCode = "TEST_SIGNAL",
                    title = "Tag $id",
                    explanation = "signal",
                    weight = score
                )
            )
        )
    }
}
