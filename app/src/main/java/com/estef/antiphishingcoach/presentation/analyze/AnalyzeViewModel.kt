package com.estef.antiphishingcoach.presentation.analyze

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.model.SourceApp
import com.estef.antiphishingcoach.domain.model.AnalyzeExecutionResult
import com.estef.antiphishingcoach.domain.model.AnalyzeRequest
import com.estef.antiphishingcoach.domain.model.RecommendationCatalog
import com.estef.antiphishingcoach.domain.usecase.AnalyzeAndPersistUseCase
import com.estef.antiphishingcoach.domain.usecase.ExtractTextFromImageUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.presentation.common.StringResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del flujo Analizar:
 * - analisis por texto manual
 * - analisis desde captura (OCR local + revision + analisis)
 */
class AnalyzeViewModel(
    private val analyzeAndPersistAction: suspend (AnalyzeRequest) -> AnalyzeExecutionResult,
    private val extractTextFromImageAction: suspend (Uri) -> String,
    observeExtremePrivacyFlow: Flow<Boolean>,
    private val stringResolver: StringResolver
) : ViewModel() {

    constructor(
        analyzeAndPersistUseCase: AnalyzeAndPersistUseCase,
        extractTextFromImageUseCase: ExtractTextFromImageUseCase,
        observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase,
        stringResolver: StringResolver
    ) : this(
        analyzeAndPersistAction = { request -> analyzeAndPersistUseCase(request) },
        extractTextFromImageAction = { uri -> extractTextFromImageUseCase(uri) },
        observeExtremePrivacyFlow = observeExtremePrivacyUseCase(),
        stringResolver = stringResolver
    )

    private val _uiState = MutableStateFlow(AnalyzeUiState())
    val uiState: StateFlow<AnalyzeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeExtremePrivacyFlow.collect { enabled ->
                _uiState.update { state -> state.copy(extremePrivacyEnabled = enabled) }
            }
        }
    }

    fun analyze(inputText: String, title: String?, sourceApp: SourceApp) {
        val sanitizedInput = inputText.trim()
        if (sanitizedInput.isBlank()) {
            val message = stringResolver.get(R.string.analyze_error_input_required)
            setError(message = message, inputError = message)
            return
        }

        _uiState.update { state ->
            state.copy(
                isLoading = true,
                inputError = null,
                statusMessage = stringResolver.get(R.string.analyze_status_analyzing),
                flowState = AnalyzeFlowState.Analyzing
            )
        }

        viewModelScope.launch {
            try {
                val startMs = System.currentTimeMillis()
                val result = analyzeAndPersistAction(
                    AnalyzeRequest(
                        inputText = sanitizedInput,
                        title = title,
                        sourceApp = sourceApp
                    )
                )
                val elapsedMs = System.currentTimeMillis() - startMs
                logDebug("Analisis completado en ${elapsedMs}ms")
                publishAnalysisResult(result)
            } catch (error: Exception) {
                setError(stringResolver.get(R.string.analyze_error_analysis_failed))
                logError("Error al analizar texto", error)
            }
        }
    }

    fun onPickImageRequested() {
        _uiState.update { state ->
            state.copy(
                flowState = AnalyzeFlowState.PickingImage,
                inputError = null,
                statusMessage = null
            )
        }
    }

    fun onImageSelectionCancelled() {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                flowState = AnalyzeFlowState.Idle,
                statusMessage = stringResolver.get(R.string.analyze_status_image_selection_cancelled)
            )
        }
    }

    fun onImageSelected(uri: Uri?) {
        if (uri == null) {
            onImageSelectionCancelled()
            return
        }
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                flowState = AnalyzeFlowState.OcrRunning,
                inputError = null,
                statusMessage = stringResolver.get(R.string.analyze_status_ocr_processing)
            )
        }

        viewModelScope.launch {
            try {
                val ocrText = extractTextFromImageAction(uri)
                if (ocrText.isBlank()) {
                    setError(stringResolver.get(R.string.analyze_error_no_text_detected))
                    return@launch
                }
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        flowState = AnalyzeFlowState.OcrReady(ocrText),
                        statusMessage = stringResolver.get(R.string.analyze_status_ocr_ready)
                    )
                }
            } catch (error: Exception) {
                setError(stringResolver.get(R.string.analyze_error_ocr_failed))
                logError("Error en OCR local", error)
            }
        }
    }

    fun onOcrTextConfirmed(text: String, title: String?, sourceApp: SourceApp) {
        analyze(
            inputText = text,
            title = title,
            sourceApp = sourceApp
        )
    }

    fun onOcrReviewCancelled() {
        _uiState.update { state ->
            state.copy(
                flowState = AnalyzeFlowState.Idle,
                statusMessage = stringResolver.get(R.string.analyze_status_ocr_review_cancelled)
            )
        }
    }

    private fun publishAnalysisResult(result: AnalyzeExecutionResult) {
        val recommendations = RecommendationCatalog.fromCodes(result.output.recommendationCodes)
        val statusMessage = if (result.usedExtremePrivacy) {
            stringResolver.get(R.string.analyze_status_result_privacy_on)
        } else {
            stringResolver.get(R.string.analyze_status_result_saved)
        }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                statusMessage = statusMessage,
                flowState = AnalyzeFlowState.ResultReady,
                result = AnalysisPresentation(
                    score = result.output.score,
                    trafficLight = result.output.trafficLight,
                    sourceTypeLabel = result.output.sourceType.name,
                    sanitizedDomain = result.output.sanitizedDomain,
                    signals = result.output.signals,
                    recommendations = recommendations,
                    persistedIncidentId = result.persistedIncidentId
                )
            )
        }
    }

    private fun setError(message: String, inputError: String? = null) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                inputError = inputError,
                statusMessage = message,
                flowState = AnalyzeFlowState.Error(message)
            )
        }
    }

    private companion object {
        private const val TAG = "AnalyzeViewModel"
    }

    private fun logDebug(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    private fun logError(message: String, throwable: Throwable) {
        runCatching { Log.e(TAG, message, throwable) }
    }
}
