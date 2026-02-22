package com.estef.antiphishingcoach.domain.usecase

import android.util.Log
import com.estef.antiphishingcoach.core.common.DispatcherProvider
import com.estef.antiphishingcoach.domain.model.AnalyzeExecutionResult
import com.estef.antiphishingcoach.domain.model.AnalyzeRequest
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.repository.SettingsRepository
import kotlinx.coroutines.withContext

/**
 * Ejecuta analisis y guarda metadatos solo si privacidad extrema esta desactivada.
 */
class AnalyzeAndPersistUseCase(
    private val analyzeInputUseCase: AnalyzeInputUseCase,
    private val incidentRepository: IncidentRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatcherProvider = DispatcherProvider()
) {
    suspend operator fun invoke(request: AnalyzeRequest): AnalyzeExecutionResult {
        val totalStartMs = System.currentTimeMillis()
        val analysisStartMs = System.currentTimeMillis()
        val output = withContext(dispatchers.default) {
            analyzeInputUseCase(request.inputText)
        }
        val analysisElapsedMs = System.currentTimeMillis() - analysisStartMs
        logDebug("Analisis heuristico completado en ${analysisElapsedMs}ms")

        val extremePrivacyEnabled = settingsRepository.isExtremePrivacyEnabled()
        val persistenceStartMs = System.currentTimeMillis()
        val incidentId = if (extremePrivacyEnabled) {
            null
        } else {
            withContext(dispatchers.io) {
                incidentRepository.saveIncident(
                    IncidentRecord(
                        id = 0L,
                        createdAt = System.currentTimeMillis(),
                        title = request.title?.takeIf { it.isNotBlank() },
                        sourceType = output.sourceType.name,
                        sourceApp = request.sourceApp.name,
                        scenarioType = request.scenarioType?.name,
                        trafficLight = output.trafficLight.name,
                        score = output.score,
                        sanitizedDomain = output.sanitizedDomain,
                        recommendationCodes = output.recommendationCodes,
                        signals = output.signals
                    )
                )
            }
        }
        val persistenceElapsedMs = System.currentTimeMillis() - persistenceStartMs
        val totalElapsedMs = System.currentTimeMillis() - totalStartMs
        logDebug(
            "Pipeline analisis+persistencia en ${totalElapsedMs}ms " +
                "(persistencia=${persistenceElapsedMs}ms, modoExtremo=$extremePrivacyEnabled)"
        )
        return AnalyzeExecutionResult(
            output = output,
            persistedIncidentId = incidentId,
            usedExtremePrivacy = extremePrivacyEnabled
        )
    }

    private fun logDebug(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    private companion object {
        private const val TAG = "AnalyzeAndPersistUC"
    }
}
