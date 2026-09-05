package com.insaner.fonecheck.export

import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.presentationConfidence
import com.insaner.fonecheck.domain.model.presentationReason
import com.insaner.fonecheck.localization.shouldShowEvidenceReason
import java.time.Duration
import java.time.Instant

enum class PdfTextStyle(
    val maxCharacters: Int,
    val lineHeight: Int,
) {
    TITLE(42, 34),
    HEADING(60, 25),
    CATEGORY(66, 23),
    BODY(86, 18),
    MONO(78, 17),
}

data class PdfTextBlock(
    val text: String,
    val style: PdfTextStyle,
)

data class PdfTextLine(
    val text: String,
    val style: PdfTextStyle,
)

object PdfLayoutEngine {
    fun paginate(
        blocks: List<PdfTextBlock>,
        contentHeight: Int = 700,
    ): List<List<PdfTextLine>> {
        require(contentHeight >= PdfTextStyle.TITLE.lineHeight)
        val pages = mutableListOf<MutableList<PdfTextLine>>()
        var page = mutableListOf<PdfTextLine>()
        var usedHeight = 0
        blocks.forEach { block ->
            wrap(block.text, block.style.maxCharacters).forEach { text ->
                if (page.isNotEmpty() && usedHeight + block.style.lineHeight > contentHeight) {
                    pages += page
                    page = mutableListOf()
                    usedHeight = 0
                }
                page += PdfTextLine(text, block.style)
                usedHeight += block.style.lineHeight
            }
        }
        if (page.isNotEmpty()) pages += page
        return pages.ifEmpty { listOf(emptyList()) }
    }

    private fun wrap(
        text: String,
        maxCharacters: Int,
    ): List<String> {
        if (text.isBlank()) return listOf("")
        val lines = mutableListOf<String>()
        var current = ""
        text.trim().split(Regex("\\s+")).forEach { word ->
            val pieces = word.chunked(maxCharacters)
            pieces.forEachIndexed { index, piece ->
                val candidate = if (current.isEmpty()) piece else "$current $piece"
                if (candidate.length <= maxCharacters) {
                    current = candidate
                } else {
                    lines += current
                    current = piece
                }
                if (index < pieces.lastIndex) {
                    lines += current
                    current = ""
                }
            }
        }
        if (current.isNotEmpty()) lines += current
        return lines
    }
}

data class PdfReportLabels(
    val title: String,
    val reportId: String,
    val reportFormat: String,
    val scoreVersion: String,
    val app: String,
    val device: String,
    val android: String,
    val completed: String,
    val duration: String,
    val score: String,
    val scoreState: String,
    val coverage: String,
    val counts: String,
    val categories: String,
    val source: String,
    val confidence: String,
    val reason: String,
    val captured: String,
    val readAt: String,
    val disclaimer: String,
    val scope: (DiagnosticReport) -> String,
    val scoreScopeNote: String,
    val categoryName: (DiagnosticCategoryId) -> String,
    val checkName: (DiagnosticCheckId) -> String,
    val statusName: (DiagnosticStatus) -> String,
    val scoreStateName: (ScoreState) -> String,
    val sourceName: (EvidenceSource) -> String,
    val confidenceName: (Confidence) -> String,
    val reasonName: (EvidenceReasonCode) -> String,
    val stableTextName: (String) -> String,
    val booleanValue: (Boolean) -> String,
    val numberValue: (Number) -> String,
    val unitName: (EvidenceUnitCode) -> String,
    val countsValue: (CoverageSummary, Int, Int) -> String,
    val completedValue: (Instant) -> String,
    val durationValue: (Duration) -> String,
) {
    companion object {
        fun english() =
            PdfReportLabels(
                title = "fonecheck diagnostic report",
                reportId = "Report ID",
                reportFormat = "Report format",
                scoreVersion = "Score version",
                app = "App",
                device = "Device",
                android = "Android",
                completed = "Completed",
                duration = "Duration",
                score = "Score",
                scoreState = "Score state",
                coverage = "Coverage",
                counts = "Counts",
                categories = "Diagnostic categories",
                source = "Source",
                confidence = "Confidence",
                reason = "Reason",
                captured = "Captured",
                readAt = "Read or received",
                disclaimer = "Differences and measurements do not prove physical device health.",
                scope = { report ->
                    if (report.kind == ReportKind.FULL_CHECK) {
                        "Scope: saved Full Check observations."
                    } else {
                        "Scope: ${report.categories.single().categoryId.name.lowercase()} only."
                    }
                },
                scoreScopeNote =
                    "Scores summarize rated observations. Coverage includes informational observations " +
                        "and excludes unavailable or inapplicable observations; " +
                        "it does not certify physical condition.",
                categoryName = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                checkName = { it.value },
                statusName = {
                    if (it == DiagnosticStatus.NOT_TESTED) {
                        "not measured"
                    } else {
                        it.name.lowercase()
                    }
                },
                scoreStateName = { it.name.lowercase() },
                sourceName = { it.name.lowercase() },
                confidenceName = { it.name.lowercase() },
                reasonName = { it.value.replace('_', ' ') },
                stableTextName = { it.replace('_', ' ') },
                booleanValue = { if (it) "yes" else "no" },
                numberValue = Number::toString,
                unitName = ::englishUnitName,
                countsValue = { coverage, warnings, failures ->
                    "${coverage.completedCount} completed, ${coverage.notTestedCount} not measured, " +
                        "${coverage.unavailableCount} unavailable, $warnings warnings, $failures failures"
                },
                completedValue = Instant::toString,
                durationValue = { "${it.seconds}s" },
            )
    }
}

object ReportPdfContentBuilder {
    fun build(
        report: DiagnosticReport,
        labels: PdfReportLabels,
    ): List<PdfTextBlock> {
        val categories = report.categories.associateBy { it.categoryId }
        val includedCategories =
            DiagnosticCatalog.categories.filter {
                report.kind == ReportKind.FULL_CHECK ||
                    it in categories
            }
        val evidence = report.categories.flatMap { it.evidence }
        val duration = Duration.between(report.startedAt, report.completedAt).coerceAtLeast(Duration.ZERO)
        return buildList {
            add(PdfTextBlock(labels.title, PdfTextStyle.TITLE))
            add(PdfTextBlock("${labels.reportId}: ${report.stableId}", PdfTextStyle.MONO))
            add(PdfTextBlock("${labels.reportFormat}: ${report.schemaVersion.value}", PdfTextStyle.BODY))
            add(PdfTextBlock("${labels.scoreVersion}: ${report.score.version.value}", PdfTextStyle.BODY))
            add(
                PdfTextBlock(
                    "${labels.app}: ${report.app.versionName} (${report.app.versionCode})",
                    PdfTextStyle.BODY,
                ),
            )
            add(
                PdfTextBlock(
                    "${labels.device}: ${report.device.manufacturer} ${report.device.model}",
                    PdfTextStyle.BODY,
                ),
            )
            add(
                PdfTextBlock(
                    "${labels.android}: ${report.device.androidRelease} (API ${report.device.apiLevel})",
                    PdfTextStyle.BODY,
                ),
            )
            add(PdfTextBlock("${labels.completed}: ${labels.completedValue(report.completedAt)}", PdfTextStyle.BODY))
            add(PdfTextBlock("${labels.duration}: ${labels.durationValue(duration)}", PdfTextStyle.BODY))
            add(PdfTextBlock(labels.scope(report), PdfTextStyle.BODY))
            add(PdfTextBlock(labels.scoreScopeNote, PdfTextStyle.BODY))
            add(
                PdfTextBlock(
                    "${labels.score}: ${report.score.value ?: "—"}",
                    PdfTextStyle.HEADING,
                ),
            )
            add(
                PdfTextBlock(
                    "${labels.scoreState}: ${labels.scoreStateName(report.score.state)}",
                    PdfTextStyle.BODY,
                ),
            )
            add(PdfTextBlock("${labels.coverage}: ${report.coverage.percentage}%", PdfTextStyle.HEADING))
            add(
                PdfTextBlock(
                    "${labels.counts}: " +
                        labels.countsValue(
                            report.coverage,
                            evidence.count { it.status == DiagnosticStatus.WARNING },
                            evidence.count { it.status == DiagnosticStatus.FAIL },
                        ),
                    PdfTextStyle.BODY,
                ),
            )
            add(PdfTextBlock(labels.disclaimer, PdfTextStyle.BODY))
            add(PdfTextBlock(labels.categories, PdfTextStyle.HEADING))
            includedCategories.forEach { categoryId ->
                addCategory(categoryId, categories[categoryId], labels)
            }
        }
    }

    private fun MutableList<PdfTextBlock>.addCategory(
        categoryId: DiagnosticCategoryId,
        category: DiagnosticCategoryResult?,
        labels: PdfReportLabels,
    ) {
        val status = category?.aggregateStatus ?: DiagnosticStatus.NOT_TESTED
        add(
            PdfTextBlock(
                "${labels.categoryName(categoryId)} — ${labels.statusName(status)}",
                PdfTextStyle.CATEGORY,
            ),
        )
        category?.evidence.orEmpty().forEach { item ->
            add(
                PdfTextBlock(
                    "${labels.checkName(item.checkId)} — ${labels.statusName(item.status)}",
                    PdfTextStyle.MONO,
                ),
            )
            item.value?.let {
                add(
                    PdfTextBlock(
                        "${valueText(it, labels)}" +
                            item.unit
                                ?.let(labels.unitName)
                                ?.takeIf(String::isNotBlank)
                                ?.let { unit -> " $unit" }
                                .orEmpty(),
                        PdfTextStyle.MONO,
                    ),
                )
            }
            add(PdfTextBlock("${labels.source}: ${labels.sourceName(item.source)}", PdfTextStyle.BODY))
            add(
                PdfTextBlock(
                    "${labels.confidence}: ${labels.confidenceName(item.presentationConfidence())}",
                    PdfTextStyle.BODY,
                ),
            )
            item.presentationReason()?.takeIf { shouldShowEvidenceReason(item.status, it) }?.let {
                add(PdfTextBlock("${labels.reason}: ${labels.reasonName(it)}", PdfTextStyle.BODY))
            }
            val timeLabel =
                if (item.categoryId == DiagnosticCategoryId.THERMAL) {
                    labels.readAt
                } else {
                    labels.captured
                }
            add(
                PdfTextBlock(
                    "$timeLabel: ${labels.completedValue(item.capturedAt)}",
                    PdfTextStyle.BODY,
                ),
            )
        }
    }

    private fun valueText(
        value: EvidenceValue,
        labels: PdfReportLabels,
    ): String =
        when (value) {
            is EvidenceValue.BooleanValue -> labels.booleanValue(value.value)
            is EvidenceValue.IntValue -> labels.numberValue(value.value)
            is EvidenceValue.LongValue -> labels.numberValue(value.value)
            is EvidenceValue.DecimalValue -> labels.numberValue(value.value)
            is EvidenceValue.DoubleValue -> labels.numberValue(value.value)
            is EvidenceValue.RawTextValue -> value.value
            is EvidenceValue.StableTextCodeValue -> labels.stableTextName(value.value)
        }
}

private fun englishUnitName(unit: EvidenceUnitCode): String =
    when (unit.value) {
        "bytes" -> "bytes"
        "celsius" -> "°C"
        "count", "ratio" -> ""
        "mebibytes_per_second" -> "MiB/s"
        "milliamperes" -> "mA"
        "milliseconds" -> "ms"
        "operations_per_second" -> "operations/s"
        "percent" -> "%"
        "pixels" -> "px"
        "samples" -> "samples"
        else -> unit.value.replace('_', ' ')
    }
