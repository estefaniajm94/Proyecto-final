package com.estef.antiphishingcoach.core.export

import com.estef.antiphishingcoach.domain.model.AnalysisOutput

/**
 * Generador simple de reportes en formato texto/markdown.
 * La exportación a SAF/Intent se implementa en una iteración posterior.
 */
class ReportExporter {
    fun toMarkdown(output: AnalysisOutput): String {
        return buildString {
            appendLine("# Reporte de análisis")
            appendLine("- Score: ${output.score}")
            appendLine("- Semáforo: ${output.trafficLight}")
            appendLine("- Tipo fuente: ${output.sourceType}")
            appendLine("- Dominio sanitizado: ${output.sanitizedDomain ?: "N/A"}")
            appendLine("## Señales")
            output.signals.forEach { signal ->
                appendLine("- [${signal.signalCode}] ${signal.title} (${signal.weight})")
            }
        }
    }
}
