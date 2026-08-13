package com.insaner.fonecheck.export

import android.content.Context
import androidx.core.content.FileProvider
import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.domain.model.DiagnosticReport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ReportExporter {
        override suspend fun exportPdf(report: DiagnosticReport): ExportedReport =
            withContext(ioDispatcher) {
                val target = prepareTarget(report, "pdf")
                try {
                    target.temporaryFile
                        .outputStream()
                        .buffered()
                        .use { pdfRenderer.render(report, it) }
                    finalizeExport(target, "PDF")
                } finally {
                    target.deleteTemporaryFile()
                }
                target.toExportedReport(PDF_MIME_TYPE)
            }

        override suspend fun exportJson(report: DiagnosticReport): ExportedReport =
            withContext(ioDispatcher) {
                val target = prepareTarget(report, "json")
                try {
                    target.temporaryFile.writeText(ReportPayloadCodec.encode(report), Charsets.UTF_8)
                    finalizeExport(target, "JSON")
                } finally {
                    target.deleteTemporaryFile()
                }
                target.toExportedReport(JSON_MIME_TYPE)
            }

        private fun prepareTarget(
            report: DiagnosticReport,
            extension: String,
        ): ExportTarget {
            val exportRoot = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
            cleanupOldExports(exportRoot)
            val safeId = report.stableId.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val displayName = "fonecheck-$safeId.$extension"
            return ExportTarget(
                displayName = displayName,
                outputFile = File(exportRoot, displayName),
                temporaryFile = File(exportRoot, "$displayName.tmp"),
            )
        }

        private fun finalizeExport(
            target: ExportTarget,
            format: String,
        ) {
            if (target.outputFile.exists()) {
                check(target.outputFile.delete()) { "Could not replace $format export." }
            }
            check(target.temporaryFile.renameTo(target.outputFile)) { "Could not finalize $format export." }
        }

        private fun ExportTarget.deleteTemporaryFile() {
            if (temporaryFile.exists()) {
                check(temporaryFile.delete()) { "Could not delete temporary export." }
            }
        }

        private fun ExportTarget.toExportedReport(mimeType: String): ExportedReport =
            ExportedReport(
                uri =
                    FileProvider
                        .getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            outputFile,
                        ).toString(),
                mimeType = mimeType,
                displayName = displayName,
            )

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

        private data class ExportTarget(
            val displayName: String,
            val outputFile: File,
            val temporaryFile: File,
        )
    }

class FonecheckFileProvider : FileProvider()
