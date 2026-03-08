package com.estef.antiphishingcoach.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.estef.antiphishingcoach.core.export.ReportExporter
import com.estef.antiphishingcoach.domain.model.IncidentRecord
import java.io.File

data class ExportedReportFile(
    val uri: Uri,
    val fileName: String
)

/**
 * Genera un archivo Markdown en cache y devuelve Uri content:// para compartir.
 *
 * Vive en la capa data porque depende de Context y FileProvider.
 */
class ExportReportToFileUseCase(
    private val appContext: Context,
    private val reportExporter: ReportExporter = ReportExporter()
) {
    operator fun invoke(incident: IncidentRecord): ExportedReportFile {
        val markdown = reportExporter.buildMarkdown(incident)
        val reportsDir = File(appContext.cacheDir, REPORT_DIR)
        val fileName = "report_${incident.id}_${System.currentTimeMillis()}.md"
        val file = reportExporter.writeMarkdownFile(reportsDir, fileName, markdown)
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
        return ExportedReportFile(uri = uri, fileName = fileName)
    }

    private companion object {
        private const val REPORT_DIR = "reports"
    }
}
