package com.insaner.fonecheck.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.localization.evidenceLabelStringRes
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.localization.stableCodeDisplayText
import com.insaner.fonecheck.localization.stableTextStringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
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
            val pages =
                PdfLayoutEngine.paginate(
                    ReportPdfContentBuilder.build(report, labels()),
                    contentHeight = CONTENT_HEIGHT,
                )
            val document = PdfDocument()
            try {
                pages.forEachIndexed { index, lines ->
                    val pageNumber = index + 1
                    val page =
                        document.startPage(
                            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
                        )
                    drawPage(page, lines, pageNumber, pages.size)
                    document.finishPage(page)
                }
                document.writeTo(output)
            } finally {
                document.close()
            }
            return PdfRenderResult(pages.size)
        }

        private fun drawPage(
            page: PdfDocument.Page,
            lines: List<PdfTextLine>,
            pageNumber: Int,
            pageCount: Int,
        ) {
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)
            val brandPaint = paint(PdfTextStyle.HEADING).apply { color = Color.rgb(0, 113, 109) }
            canvas.drawText("fonecheck", MARGIN, HEADER_BASELINE, brandPaint)
            var baseline = CONTENT_TOP
            lines.forEach { line ->
                baseline += line.style.lineHeight
                canvas.drawText(line.text, MARGIN, baseline, paint(line.style))
            }
            val footerPaint = paint(PdfTextStyle.BODY).apply { color = Color.DKGRAY }
            canvas.drawText(
                context.getString(R.string.pdf_page, pageNumber, pageCount),
                MARGIN,
                FOOTER_BASELINE,
                footerPaint,
            )
        }

        private fun paint(style: PdfTextStyle): Paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(28, 32, 40)
                textSize =
                    when (style) {
                        PdfTextStyle.TITLE -> 22f
                        PdfTextStyle.HEADING -> 15f
                        PdfTextStyle.CATEGORY -> 13f
                        PdfTextStyle.BODY -> 10f
                        PdfTextStyle.MONO -> 9f
                    }
                typeface =
                    when (style) {
                        PdfTextStyle.TITLE,
                        PdfTextStyle.HEADING,
                        PdfTextStyle.CATEGORY,
                        -> Typeface.create("sans-serif-medium", Typeface.NORMAL)

                        PdfTextStyle.MONO -> Typeface.MONOSPACE
                        PdfTextStyle.BODY -> Typeface.DEFAULT
                    }
            }

        private fun labels() =
            PdfReportLabels(
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
                disclaimer = context.getString(R.string.pdf_disclaimer),
                categoryName = { context.getString(it.labelResource()) },
                checkName = { checkId ->
                    evidenceLabelStringRes(checkId.value)?.let(context::getString)
                        ?: stableCodeDisplayText(checkId.value.substringAfter('.'))
                },
                statusName = { context.getString(it.labelResource()) },
                scoreStateName = { context.getString(it.labelResource()) },
                sourceName = { context.getString(it.labelResource()) },
                confidenceName = { context.getString(it.labelResource()) },
                reasonName = { reason ->
                    evidenceReasonStringRes(reason)?.let(context::getString)
                        ?: stableCodeDisplayText(reason.value)
                },
                stableTextName = { code ->
                    stableTextStringRes(code)?.let(context::getString) ?: stableCodeDisplayText(code)
                },
                numberValue = NumberFormat.getNumberInstance(Locale.getDefault())::format,
                unitName = { unit -> localizedUnitName(context, unit) },
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
                completedValue =
                    DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.getDefault())
                        .withZone(ZoneId.systemDefault())::format,
                durationValue = {
                    context.getString(
                        R.string.report_duration_value,
                        it.toMinutes().toString(),
                        (it.seconds % 60).toString(),
                    )
                },
            )

        private companion object {
            const val PAGE_WIDTH = 595
            const val PAGE_HEIGHT = 842
            const val MARGIN = 42f
            const val HEADER_BASELINE = 43f
            const val CONTENT_TOP = 58f
            const val CONTENT_HEIGHT = 700
            const val FOOTER_BASELINE = 820f
        }
    }

private fun DiagnosticCategoryId.labelResource(): Int =
    when (this) {
        DiagnosticCategoryId.DEVICE -> R.string.home_cat_device
        DiagnosticCategoryId.PERFORMANCE -> R.string.home_cat_performance
        DiagnosticCategoryId.SIM -> R.string.home_cat_sim
        DiagnosticCategoryId.DISPLAY -> R.string.home_cat_display
        DiagnosticCategoryId.AUDIO -> R.string.home_cat_audio
        DiagnosticCategoryId.CAMERA -> R.string.home_cat_camera
        DiagnosticCategoryId.SENSORS -> R.string.home_cat_sensors
        DiagnosticCategoryId.CONNECTIVITY -> R.string.home_cat_connectivity
        DiagnosticCategoryId.BATTERY -> R.string.home_cat_battery
        DiagnosticCategoryId.THERMAL -> R.string.home_cat_thermal
        DiagnosticCategoryId.STORAGE -> R.string.home_cat_storage
        DiagnosticCategoryId.VIBRATION -> R.string.home_cat_vibration
        DiagnosticCategoryId.BUTTONS -> R.string.home_cat_buttons
        DiagnosticCategoryId.BIOMETRICS -> R.string.home_cat_biometrics
    }

private fun DiagnosticStatus.labelResource(): Int =
    when (this) {
        DiagnosticStatus.PASS -> R.string.run_all_status_pass
        DiagnosticStatus.FAIL -> R.string.run_all_status_fail
        DiagnosticStatus.WARNING -> R.string.run_all_status_warning
        DiagnosticStatus.INFO -> R.string.run_all_status_info
        DiagnosticStatus.NOT_AVAILABLE -> R.string.run_all_status_unavailable
        DiagnosticStatus.NOT_TESTED -> R.string.status_not_measured
    }

private fun ScoreState.labelResource(): Int =
    when (this) {
        ScoreState.INCOMPLETE -> R.string.report_score_incomplete
        ScoreState.PARTIAL -> R.string.report_score_partial
        ScoreState.COMPLETE -> R.string.report_score_complete
    }

private fun EvidenceSource.labelResource(): Int =
    when (this) {
        EvidenceSource.AUTOMATIC_MEASUREMENT -> R.string.report_source_automatic
        EvidenceSource.ANDROID_API -> R.string.report_source_android_api
        EvidenceSource.USER_CONFIRMATION -> R.string.report_source_user
        EvidenceSource.DERIVED -> R.string.report_source_derived
        EvidenceSource.ESTIMATE -> R.string.report_source_estimate
    }

private fun Confidence.labelResource(): Int =
    when (this) {
        Confidence.HIGH -> R.string.confidence_high
        Confidence.LOW -> R.string.confidence_low
        Confidence.UNAVAILABLE -> R.string.confidence_unavailable
    }

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
