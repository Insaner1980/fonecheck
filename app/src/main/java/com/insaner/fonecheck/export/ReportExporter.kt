package com.insaner.fonecheck.export

import android.content.Context
import androidx.core.content.FileProvider
import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.domain.model.DiagnosticReport
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

data class ExportedReport(
    val uri: String,
    val mimeType: String,
    val displayName: String,
)

interface ReportExporter {
    suspend fun exportPdf(report: DiagnosticReport): ExportedReport

    suspend fun exportJson(report: DiagnosticReport): ExportedReport
}

class AndroidReportExporter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val pdfRenderer: ReportPdfRenderer,
    ) : ReportExporter {
        override suspend fun exportPdf(report: DiagnosticReport): ExportedReport {
            val exportRoot = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
            cleanupOldExports(exportRoot)
            val safeId = report.stableId.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val displayName = "fonecheck-$safeId.pdf"
            val outputFile = File(exportRoot, displayName)
            val temporaryFile = File(exportRoot, "$displayName.tmp")
            try {
                temporaryFile.outputStream().buffered().use { pdfRenderer.render(report, it) }
                if (outputFile.exists()) check(outputFile.delete()) { "Could not replace PDF export." }
                check(temporaryFile.renameTo(outputFile)) { "Could not finalize PDF export." }
            } finally {
                if (temporaryFile.exists()) temporaryFile.delete()
            }
            return ExportedReport(
                uri =
                    FileProvider
                        .getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            outputFile,
                        ).toString(),
                mimeType = PDF_MIME_TYPE,
                displayName = displayName,
            )
        }

        override suspend fun exportJson(report: DiagnosticReport): ExportedReport {
            val exportRoot = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
            cleanupOldExports(exportRoot)
            val safeId = report.stableId.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val displayName = "fonecheck-$safeId.json"
            val outputFile = File(exportRoot, displayName)
            val temporaryFile = File(exportRoot, "$displayName.tmp")
            try {
                temporaryFile.writeText(ReportPayloadCodec.encode(report), Charsets.UTF_8)
                if (outputFile.exists()) check(outputFile.delete()) { "Could not replace JSON export." }
                check(temporaryFile.renameTo(outputFile)) { "Could not finalize JSON export." }
            } finally {
                if (temporaryFile.exists()) temporaryFile.delete()
            }
            return ExportedReport(
                uri =
                    FileProvider
                        .getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            outputFile,
                        ).toString(),
                mimeType = JSON_MIME_TYPE,
                displayName = displayName,
            )
        }

        private fun cleanupOldExports(exportRoot: File) {
            val cutoff = System.currentTimeMillis() - EXPORT_RETENTION_MILLIS
            exportRoot
                .listFiles()
                .orEmpty()
                .filter { it.lastModified() < cutoff }
                .forEach(File::delete)
        }

        private companion object {
            const val EXPORT_DIRECTORY = "report-exports"
            const val PDF_MIME_TYPE = "application/pdf"
            const val JSON_MIME_TYPE = "application/json"
            const val EXPORT_RETENTION_MILLIS = 24L * 60L * 60L * 1000L
        }
    }

class FonecheckFileProvider : FileProvider()
