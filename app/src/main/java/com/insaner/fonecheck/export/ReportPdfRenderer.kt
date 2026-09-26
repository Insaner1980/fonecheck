package com.insaner.fonecheck.export

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.localization.confidenceStringRes
import com.insaner.fonecheck.localization.diagnosticCategoryStringRes
import com.insaner.fonecheck.localization.diagnosticStatusStringRes
import com.insaner.fonecheck.localization.evidenceLabelResource
import com.insaner.fonecheck.localization.evidenceSourceStringRes
import com.insaner.fonecheck.localization.scoreStateStringRes
import com.insaner.fonecheck.localization.stableCodeDisplayText
import com.insaner.fonecheck.localization.stableTextStringRes
import com.insaner.fonecheck.ui.format.formatPdfDateTime
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import javax.inject.Inject

data class PdfRenderResult(
    val pageCount: Int,
)

class ReportPdfRenderer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun render(
            report: DiagnosticReport,
            output: OutputStream,
        ): PdfRenderResult {
            val languageContext = ContextCompat.getContextForLanguage(context)
            val configuration = android.content.res.Configuration(languageContext.resources.configuration)
            val localizedContext = languageContext.createConfigurationContext(configuration)
            val labels = labels(localizedContext)
            val layout = AndroidPdfLayout(localizedContext, labels)
            val pages = layout.paginate(ReportPdfContentBuilder.build(report, labels))
            val document = PdfDocument()
            try {
                pages.forEachIndexed { index, rows ->
                    val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, index + 1).create())
                    try {
                        val canvas = page.canvas
                        canvas.drawColor(Color.WHITE)
                        if (index == 0) {
                            layout.drawLogo(canvas)
                        } else {
                            layout.drawMarginText(canvas, "fonecheck / ${labels.title}", 42f, 27f, 511)
                        }
                        rows.forEach { layout.drawRow(canvas, it, 42f, AndroidPdfLayout.CONTENT_TOP) }
                        layout.drawMarginText(canvas, "${labels.reportId}: ${report.stableId.take(8)}…", 42f, 800f, 330)
                        layout.drawMarginText(
                            canvas,
                            localizedContext.getString(R.string.pdf_page, index + 1, pages.size),
                            382f,
                            800f,
                            171,
                        )
                    } finally {
                        document.finishPage(page)
                    }
                }
                document.writeTo(output)
            } finally {
                document.close()
            }
            return PdfRenderResult(pages.size)
        }

        internal fun labels(context: Context = ContextCompat.getContextForLanguage(this.context)): PdfReportLabels {
            val locale = context.resources.configuration.locales[0]
            val zone = java.time.ZoneId.systemDefault()
            val fileContext = pdfFileContext(context, locale)
            return PdfReportLabels(
                findings = context.getString(R.string.pdf_findings),
                details = context.getString(R.string.pdf_details),
                notes = context.getString(R.string.pdf_notes),
                observation = context.getString(R.string.pdf_observation),
                result = context.getString(R.string.pdf_result),
                status = context.getString(R.string.pdf_status),
                completedApplicable = context.getString(R.string.pdf_completed_applicable),
                excluded = context.getString(R.string.pdf_excluded),
                notMeasured = context.getString(R.string.pdf_not_measured),
                warnings = context.getString(R.string.pdf_warnings),
                failures = context.getString(R.string.pdf_failures),
                noFailures = context.getString(R.string.pdf_no_failures),
                findingsReference = context.getString(R.string.pdf_findings_reference),
                completedNote = context.getString(R.string.pdf_completed_note),
                interpretation = context.getString(R.string.pdf_interpretation),
                continued = context.getString(R.string.pdf_continued),
                securityPatch = context.getString(R.string.label_security_patch),
                emptyEvidence = context.getString(R.string.report_no_saved_evidence),
                fileSizeValue = {
                    android.text.format.Formatter
                        .formatFileSize(fileContext, it)
                },
                title = context.getString(R.string.pdf_title),
                reportId = context.getString(R.string.report_identifier),
                reportFormat = context.getString(R.string.pdf_report_format),
                scoreVersion = context.getString(R.string.pdf_score_version),
                app = context.getString(R.string.pdf_app),
                device = context.getString(R.string.pdf_device),
                android = context.getString(R.string.pdf_android),
                completed = context.getString(R.string.pdf_completed),
                duration = context.getString(R.string.pdf_duration),
                score = context.getString(R.string.pdf_score),
                scoreState = context.getString(R.string.pdf_score_state),
                coverage = context.getString(R.string.pdf_coverage),
                counts = context.getString(R.string.pdf_counts),
                categories = context.getString(R.string.pdf_categories),
                source = context.getString(R.string.pdf_source),
                confidence = context.getString(R.string.pdf_confidence),
                reason = context.getString(R.string.pdf_reason),
                captured = context.getString(R.string.pdf_captured),
                readAt = context.getString(R.string.report_read_at),
                disclaimer = context.getString(R.string.pdf_limitation),
                scope = { report -> pdfScope(context, report) },
                scoreScopeNote = context.getString(R.string.report_score_scope_note),
                timeSemantics = context.getString(R.string.report_time_semantics),
                categoryName = { context.getString(diagnosticCategoryStringRes(it)) },
                checkName = { evidence ->
                    evidenceLabelResource(evidence)?.let { resource ->
                        resource.formatArgument?.let { argument ->
                            context.getString(resource.stringResId, argument)
                        } ?: context.getString(resource.stringResId)
                    } ?: stableCodeDisplayText(evidence.checkId.value.substringAfter('.'))
                },
                statusName = { context.getString(diagnosticStatusStringRes(it)) },
                scoreStateName = { context.getString(scoreStateStringRes(it)) },
                sourceName = { context.getString(evidenceSourceStringRes(it)) },
                confidenceName = { context.getString(confidenceStringRes(it)) },
                reasonName = { reason ->
                    pdfReasonText(context, reason)
                        ?: stableCodeDisplayText(reason.value)
                },
                stableTextName = { code ->
                    stableTextStringRes(code)?.let(context::getString) ?: stableCodeDisplayText(code)
                },
                booleanValue = { value ->
                    context.getString(if (value) R.string.status_yes else R.string.status_no)
                },
                numberValue = { value ->
                    val precision = if (value is java.math.BigDecimal) value.scale().coerceAtLeast(0) else 3
                    formatUiNumber(
                        value = value,
                        locale = locale,
                        minimumFractionDigits = 0,
                        maximumFractionDigits = precision,
                        grouping = true,
                    )
                },
                unitName = { unit -> localizedUnitName(context, unit) },
                sampleCountValue = { count ->
                    context.resources.getQuantityString(
                        R.plurals.sensor_samples,
                        count,
                        formatUiNumber(count, locale, grouping = true),
                    )
                },
                countsValue = { coverage, warnings, failures ->
                    context.getString(
                        R.string.pdf_counts_value,
                        coverage.completedCount,
                        coverage.notTestedCount,
                        coverage.unavailableCount,
                        warnings,
                        failures,
                    )
                },
                completedValue = { value -> formatPdfDateTime(value, locale, zone) },
                durationValue = { value -> pdfDurationValue(context, locale, value) },
            )
        }
    }

private fun pdfFileContext(
    context: Context,
    locale: java.util.Locale,
): Context =
    context.createConfigurationContext(
        android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(uiLanguageLocale(locale))
        },
    )

private fun pdfScope(
    context: Context,
    report: DiagnosticReport,
): String =
    if (report.kind == com.insaner.fonecheck.domain.model.ReportKind.FULL_CHECK) {
        context.getString(R.string.report_scope_full)
    } else {
        context.getString(
            R.string.report_scope_category,
            context.getString(diagnosticCategoryStringRes(report.categories.single().categoryId)),
        )
    }

private fun pdfDurationValue(
    context: Context,
    locale: java.util.Locale,
    value: java.time.Duration,
): String =
    context.getString(
        R.string.report_duration_value,
        formatUiNumber(value.toMinutes(), locale),
        formatUiNumber(value.seconds % 60, locale),
    )

private fun localizedUnitName(
    context: Context,
    unit: EvidenceUnitCode,
): String =
    when (unit.value) {
        "bytes" -> "B"
        "celsius" -> "°C"
        "count", "ratio" -> ""
        "mebibytes_per_second" -> "MiB/s"
        "milliamperes" -> "mA"
        "milliseconds" -> "ms"
        "operations_per_second" -> "ops/s"
        "percent" -> "%"
        "pixels" -> "px"
        "samples" -> context.getString(R.string.pdf_unit_samples)
        else -> stableCodeDisplayText(unit.value)
    }
