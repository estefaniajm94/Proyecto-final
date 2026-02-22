package com.estef.antiphishingcoach.core.export

import com.estef.antiphishingcoach.domain.model.AnalysisOutput
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import com.estef.antiphishingcoach.domain.model.RecommendationCatalog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador de reportes Markdown basados en metadatos (sin texto original).
 */
class ReportExporter {
    fun toMarkdown(output: AnalysisOutput): String {
        val fallbackRecord = IncidentRecord(
            id = 0L,
            createdAt = System.currentTimeMillis(),
            title = null,
            sourceType = output.sourceType.name,
            sourceApp = "OTHER",
            scenarioType = null,
            trafficLight = output.trafficLight.name,
            score = output.score,
            sanitizedDomain = output.sanitizedDomain,
            recommendationCodes = output.recommendationCodes,
            signals = output.signals
        )
        return buildMarkdown(fallbackRecord)
    }

    fun buildMarkdown(incident: IncidentRecord): String {
        val generatedAt = dateFormat.format(Date(System.currentTimeMillis()))
        val incidentDate = dateFormat.format(Date(incident.createdAt))
        val recommendations = RecommendationCatalog.fromCodes(incident.recommendationCodes)
        return buildString {
            appendLine("# Reporte anti-phishing y ciberfraude")
            appendLine("- Fecha de generacion: $generatedAt")
            appendLine("- Fecha del analisis: $incidentDate")
            appendLine("- IncidentId: ${incident.id}")
            appendLine()
            appendLine("## Resumen")
            appendLine("- Score: ${incident.score}/100")
            appendLine("- Semaforo: ${incident.trafficLight}")
            appendLine("- Origen app: ${incident.sourceApp}")
            appendLine("- Tipo de fuente: ${incident.sourceType}")
            appendLine("- Escenario: ${incident.scenarioType ?: "N/A"}")
            appendLine("- Dominio sanitizado: ${incident.sanitizedDomain ?: "N/A"}")
            appendLine()
            appendLine("## Senales detectadas")
            if (incident.signals.isEmpty()) {
                appendLine("- No se registraron senales en este analisis.")
            } else {
                incident.signals.forEach { signal ->
                    appendLine("- ${signal.title} [${signal.signalCode}]")
                    appendLine("  - Explicacion: ${signal.explanation}")
                    appendLine("  - Peso: ${signal.weight}")
                }
            }
            appendLine()
            appendLine("## Recomendaciones")
            if (recommendations.isEmpty()) {
                appendLine("- Sin recomendaciones adicionales.")
            } else {
                recommendations.forEach { item ->
                    appendLine("- ${item.title}: ${item.detail}")
                }
            }
            appendLine()
            appendLine("## Avisos")
            appendLine("- No guardamos texto original del mensaje ni texto OCR en este reporte.")
            appendLine("- Herramienta educativa. No garantiza deteccion perfecta.")
            appendLine("- Verifica siempre por canales oficiales antes de actuar.")
        }
    }

    fun writeMarkdownFile(directory: File, fileName: String, markdown: String): File {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val output = File(directory, fileName)
        output.writeText(markdown, Charsets.UTF_8)
        return output
    }

    fun buildSignalTags(signals: List<DetectedSignal>, maxTags: Int = 2): List<String> {
        return signals
            .sortedByDescending { it.weight }
            .map { it.title }
            .filter { it.isNotBlank() }
            .take(maxTags)
    }

    private companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    }
}
