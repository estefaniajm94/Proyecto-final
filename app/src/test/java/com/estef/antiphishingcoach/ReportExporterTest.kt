package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.export.ReportExporter
import com.estef.antiphishingcoach.domain.model.DetectedSignal
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReportExporterTest {

    private val exporter = ReportExporter()

    @Test
    fun `markdown enriquecido no incluye texto original`() {
        val originalText = "tu contrasena es 12345"
        val incident = sampleIncident()

        val markdown = exporter.buildMarkdown(incident)

        assertTrue(markdown.contains("## Resumen"))
        assertTrue(markdown.contains("## Senales detectadas"))
        assertTrue(markdown.contains("## Recomendaciones"))
        assertTrue(markdown.contains("## Avisos"))
        assertFalse(markdown.contains(originalText))
    }

    @Test
    fun `writeMarkdownFile crea fichero con contenido`() {
        val markdown = exporter.buildMarkdown(sampleIncident())
        val tempDir = File(System.getProperty("java.io.tmpdir"), "antiphishing-export-test")
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }

        val file = exporter.writeMarkdownFile(tempDir, "report_test.md", markdown)

        assertTrue(file.exists())
        assertTrue(file.length() > 0L)
        tempDir.deleteRecursively()
    }

    private fun sampleIncident(): IncidentRecord {
        return IncidentRecord(
            id = 101L,
            createdAt = 1_700_000_000_000L,
            title = "SMS de banco",
            sourceType = "MIXED",
            sourceApp = "SMS",
            scenarioType = "BANK_IMPERSONATION",
            trafficLight = "RED",
            score = 84,
            sanitizedDomain = "seguro-banco.top",
            recommendationCodes = listOf("REC_VERIFY_DOMAIN", "REC_CALL_OFFICIAL_NUMBER"),
            signals = listOf(
                DetectedSignal(
                    signalCode = "URL_SUSPICIOUS_TLD",
                    title = "TLD de riesgo",
                    explanation = "El dominio usa extension asociada con abuso.",
                    weight = 14
                )
            )
        )
    }
}
