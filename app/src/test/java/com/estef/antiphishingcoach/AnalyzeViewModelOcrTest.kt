package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.model.SourceApp
import com.estef.antiphishingcoach.core.model.SourceType
import com.estef.antiphishingcoach.core.model.TrafficLight
import com.estef.antiphishingcoach.domain.model.AnalysisOutput
import com.estef.antiphishingcoach.domain.model.AnalyzeExecutionResult
import com.estef.antiphishingcoach.domain.model.AnalyzeRequest
import com.estef.antiphishingcoach.presentation.analyze.AnalyzeFlowState
import com.estef.antiphishingcoach.presentation.analyze.AnalyzeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AnalyzeViewModelOcrTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `flujo OCR exitoso pasa por OcrReady y acaba en ResultReady tras confirmar`() = runTest {
        var capturedRequest: AnalyzeRequest? = null
        val viewModel = AnalyzeViewModel(
            analyzeAndPersistAction = { request ->
                capturedRequest = request
                AnalyzeExecutionResult(
                    output = baseAnalysisOutput(score = 42),
                    persistedIncidentId = 33L,
                    usedExtremePrivacy = false
                )
            },
            extractTextFromImageAction = { "Texto OCR detectado" },
            observeExtremePrivacyFlow = flowOf(false),
            stringResolver = TestStringResolver()
        )

        viewModel.onImageSelected(mockk(relaxed = true))
        advanceUntilIdle()
        val ocrReadyState = viewModel.uiState.value.flowState
        assertTrue(ocrReadyState is AnalyzeFlowState.OcrReady)
        assertEquals("Texto OCR detectado", (ocrReadyState as AnalyzeFlowState.OcrReady).text)

        viewModel.onOcrTextConfirmed(
            text = "Texto OCR detectado",
            title = "captura",
            sourceApp = SourceApp.OTHER
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.flowState is AnalyzeFlowState.ResultReady)
        assertEquals(42, viewModel.uiState.value.result?.score)
        assertEquals("Texto OCR detectado", capturedRequest?.inputText)
    }

    @Test
    fun `error OCR publica estado Error`() = runTest {
        val viewModel = AnalyzeViewModel(
            analyzeAndPersistAction = { _ ->
                AnalyzeExecutionResult(
                    output = baseAnalysisOutput(score = 0),
                    persistedIncidentId = null,
                    usedExtremePrivacy = true
                )
            },
            extractTextFromImageAction = { throw IllegalStateException("OCR fail") },
            observeExtremePrivacyFlow = flowOf(false),
            stringResolver = TestStringResolver()
        )

        viewModel.onImageSelected(mockk(relaxed = true))
        advanceUntilIdle()

        val state = viewModel.uiState.value.flowState
        assertTrue(state is AnalyzeFlowState.Error)
        assertEquals(
            "No se pudo extraer texto de la imagen.",
            (state as AnalyzeFlowState.Error).message
        )
    }

    private fun baseAnalysisOutput(score: Int): AnalysisOutput {
        return AnalysisOutput(
            score = score,
            trafficLight = TrafficLight.YELLOW,
            sourceType = SourceType.TEXT,
            sanitizedDomain = null,
            recommendationCodes = emptyList(),
            signals = emptyList()
        )
    }
}
