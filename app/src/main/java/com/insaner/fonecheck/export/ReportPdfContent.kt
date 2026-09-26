package com.insaner.fonecheck.export

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.isNetworkMetadata
import com.insaner.fonecheck.domain.model.networkValueText
import com.insaner.fonecheck.domain.model.presentationConfidence
import com.insaner.fonecheck.domain.model.presentationReason
import com.insaner.fonecheck.localization.shouldShowEvidenceReason
import java.time.Duration
import java.time.Instant

/** Paper-only roles, independent of the app theme. */
enum class PdfTextStyle(
    val size: Float,
) {
    TITLE(24f),
    HEADING(14f),
    CATEGORY(13f),
    BODY(11f),
    MONO(11f),
    META(9.5f),
}

/** Adjacent blocks with the same group form one observation. Columns share a baseline. */
data class PdfTextBlock(
    val text: String,
    val style: PdfTextStyle,
    val columns: List<String> = emptyList(),
    val group: String? = null,
    val keepWithNext: Boolean = false,
    val category: String? = null,
    val startsCategory: Boolean = false,
    val endsCategories: Boolean = false,
    val finding: Boolean = false,
) {
    val allText: String get() = (listOf(text) + columns).joinToString(" ")
}

data class PdfReportLabels(
    val findings: String = "Key findings",
    val details: String = "Detailed results",
    val notes: String = "Interpretation and technical notes",
    val observation: String = "Observation",
    val result: String = "Value / result",
    val status: String = "Status",
    val completedApplicable: String = "Completed / applicable",
    val excluded: String = "Excluded from coverage",
    val notMeasured: String = "Not measured",
    val warnings: String = "Warnings",
    val failures: String = "Failures",
    val noFailures: String = "No failed observations recorded.",
    val findingsReference: String =
        "Selected findings are shown here. " +
            "All recorded observations and limitations follow in Detailed results.",
    val completedNote: String = "Completed observations include information, not only passed functional tests.",
    val interpretation: String =
        "Pass means a recorded criterion was met; fail means it was not; warning marks a recorded concern. " +
            "Info is descriptive. " +
            "Not measured has no completed result; not available is excluded. " +
            "Source describes how an observation was obtained; confidence describes its reliability. " +
            "The report ID identifies a report, not an authenticated handset. " +
            "This file is not signed or independently verified.",
    val continued: String = "Continued",
    val securityPatch: String = "Security patch",
    val absentValue: String = "n/a",
    val emptyEvidence: String = "No evidence was saved for this category.",
    val fileSizeValue: (Long) -> String = { "$it B" },
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
    val timeSemantics: String =
        "Observation times may be callback, user response or report assembly times, " +
            "including in older reports; not camera exposure times.",
    val categoryName: (DiagnosticCategoryId) -> String,
    val checkName: (DiagnosticEvidence) -> String,
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
    val sampleCountValue: (Int) -> String = { "$it samples" },
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
                disclaimer =
                    "This report summarizes observations recorded in fonecheck. " +
                        "It does not independently certify the device’s overall condition.",
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
                checkName = { it.checkId.value },
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
                reasonName = {
                    if (it.value == "user_confirmed_vibration_failure") {
                        "The user reported that vibration was not felt during the test."
                    } else {
                        it.value.replace('_', ' ')
                    }
                },
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

/** Display-only counts follow the saved-evidence coverage rules, never recalculate scores. */
data class PdfCategoryCounts(
    val completed: Int,
    val applicable: Int,
    val notMeasured: Int,
)

fun pdfCategoryCounts(category: DiagnosticCategoryResult): PdfCategoryCounts {
    val applicable =
        category.evidence.filter {
            !it.isNetworkMetadata && it.applicability == Applicability.APPLICABLE &&
                it.status != DiagnosticStatus.NOT_AVAILABLE
        }
    return PdfCategoryCounts(
        applicable.count { it.status != DiagnosticStatus.NOT_TESTED },
        applicable.size,
        applicable.count { it.status == DiagnosticStatus.NOT_TESTED },
    )
}

object ReportPdfContentBuilder {
    fun build(
        report: DiagnosticReport,
        labels: PdfReportLabels,
    ): List<PdfTextBlock> =
        buildList {
            val evidence = report.categories.flatMap { it.evidence }
            val failures = evidence.filter { it.status == DiagnosticStatus.FAIL }
            val warnings = evidence.filter { it.status == DiagnosticStatus.WARNING }

            fun line(
                text: String,
                style: PdfTextStyle = PdfTextStyle.BODY,
                finding: Boolean = false,
            ) = add(PdfTextBlock(text, style, finding = finding))

            fun number(value: Number) = labels.numberValue(value)
            line(labels.title, PdfTextStyle.TITLE)
            line(labels.scope(report))
            line("${labels.device}: ${report.device.manufacturer} ${report.device.model}")
            line("${labels.android}: ${report.device.androidRelease} (API ${number(report.device.apiLevel)})")
            report.device.securityPatch?.let { line("${labels.securityPatch}: $it", PdfTextStyle.META) }
            line("${labels.completed}: ${labels.completedValue(report.completedAt)}")
            val duration = Duration.between(report.startedAt, report.completedAt).coerceAtLeast(Duration.ZERO)
            line("${labels.duration}: ${labels.durationValue(duration)}", PdfTextStyle.META)
            val scoreValue = report.score.value?.let { "${number(it)} / ${number(100)}" } ?: labels.absentValue
            line("${labels.score}: $scoreValue", PdfTextStyle.HEADING)
            line("${labels.scoreState}: ${labels.scoreStateName(report.score.state)}")
            line("${labels.coverage}: ${number(report.coverage.percentage)}%", PdfTextStyle.HEADING)
            line(
                "${labels.completedApplicable}: " +
                    "${number(report.coverage.completedCount)}/${number(report.coverage.applicableCount)}",
                PdfTextStyle.META,
            )
            line(
                "${labels.notMeasured}: ${number(report.coverage.notTestedCount)}, " +
                    "${labels.excluded}: ${number(report.coverage.unavailableCount)}",
                PdfTextStyle.META,
            )
            line(
                "${labels.warnings}: ${number(warnings.size)}, ${labels.failures}: ${number(failures.size)}",
                PdfTextStyle.META,
            )
            line(labels.completedNote, PdfTextStyle.META)
            add(PdfTextBlock(labels.findings, PdfTextStyle.HEADING, keepWithNext = true))
            if (failures.isEmpty()) line(labels.noFailures)
            // The measured layout bounds this selection as well as this deterministic item limit.
            (failures + warnings).take(3).forEach { item ->
                val reason = item.presentationReason()?.let(labels.reasonName)
                line(
                    "${labels.statusName(item.status)}: ${labels.checkName(item)}. " +
                        "${labels.source}: ${labels.sourceName(item.source)}." + reason?.let { " $it" }.orEmpty(),
                    finding = true,
                )
            }
            report.categories.filter { pdfCategoryCounts(it).notMeasured > 0 }.take(3).forEach { category ->
                val counts = pdfCategoryCounts(category)
                line(
                    "${labels.categoryName(category.categoryId)}: ${labels.completedApplicable} " +
                        "${number(counts.completed)}/${number(counts.applicable)}, " +
                        "${labels.notMeasured}: ${number(counts.notMeasured)}",
                    PdfTextStyle.META,
                    finding = true,
                )
            }
            line(labels.findingsReference, PdfTextStyle.META)
            line(labels.disclaimer, PdfTextStyle.META)
            add(PdfTextBlock(labels.categories, PdfTextStyle.HEADING, keepWithNext = true))
            add(
                PdfTextBlock(
                    labels.categories,
                    PdfTextStyle.META,
                    columns = listOf(labels.status, labels.completedApplicable),
                    keepWithNext = true,
                ),
            )
            report.categories.forEach { category ->
                val counts = pdfCategoryCounts(category)
                add(
                    PdfTextBlock(
                        labels.categoryName(category.categoryId),
                        PdfTextStyle.BODY,
                        columns =
                            listOf(
                                labels.statusName(category.aggregateStatus),
                                "${number(counts.completed)}/${number(counts.applicable)}",
                            ),
                    ),
                )
            }
            add(PdfTextBlock(labels.details, PdfTextStyle.HEADING, keepWithNext = true))
            report.categories.forEach { category -> addCategory(category, labels) }
            add(PdfTextBlock(labels.notes, PdfTextStyle.HEADING, keepWithNext = true, endsCategories = true))
            line(labels.scoreScopeNote, PdfTextStyle.META)
            line(labels.interpretation, PdfTextStyle.META)
            line(
                DiagnosticStatus.entries.joinToString(", ") { "${it.name}: ${labels.statusName(it)}" },
                PdfTextStyle.META,
            )
            line(labels.timeSemantics, PdfTextStyle.META)
            line(labels.disclaimer, PdfTextStyle.META)
            line("${labels.reportId}: ${report.stableId}", PdfTextStyle.MONO)
            line(
                "${labels.reportFormat}: ${number(report.schemaVersion.value)}, " +
                    "${labels.scoreVersion}: ${number(report.score.version.value)}",
                PdfTextStyle.META,
            )
            line("${labels.app}: ${report.app.versionName} (${number(report.app.versionCode)})", PdfTextStyle.META)
        }

    private fun MutableList<PdfTextBlock>.addCategory(
        category: DiagnosticCategoryResult,
        labels: PdfReportLabels,
    ) {
        val counts = pdfCategoryCounts(category)
        val key = category.categoryId.name
        val heading = "${labels.categoryName(category.categoryId)}: ${labels.statusName(category.aggregateStatus)}"
        add(PdfTextBlock(heading, PdfTextStyle.CATEGORY, category = key, startsCategory = true, keepWithNext = true))
        add(
            PdfTextBlock(
                "${labels.completedApplicable}: " +
                    "${labels.numberValue(counts.completed)}/${labels.numberValue(counts.applicable)}",
                PdfTextStyle.META,
                category = key,
                keepWithNext = true,
            ),
        )
        add(
            PdfTextBlock(
                labels.observation,
                PdfTextStyle.META,
                columns = listOf(labels.result, labels.status),
                category = key,
                keepWithNext = true,
            ),
        )
        if (category.evidence.isEmpty()) {
            add(PdfTextBlock(labels.emptyEvidence, PdfTextStyle.BODY))
        }
        // Preserve every saved entry, including raw network metadata, in its historical order.
        category.evidence.forEachIndexed { index, item ->
            val group = "$key/$index"
            val value = evidenceValue(item, labels)
            add(
                PdfTextBlock(
                    labels.checkName(item),
                    PdfTextStyle.BODY,
                    columns = listOf(value, labels.statusName(item.status)),
                    group = group,
                    category = key,
                ),
            )
            add(
                PdfTextBlock(
                    "${labels.source}: ${labels.sourceName(item.source)}, " +
                        "${labels.confidence}: ${labels.confidenceName(item.presentationConfidence())}",
                    PdfTextStyle.META,
                    group = group,
                    category = key,
                ),
            )
            val timeLabel =
                if (item.categoryId == DiagnosticCategoryId.THERMAL ||
                    (item.isNetworkMetadata && item.value != null)
                ) {
                    labels.readAt
                } else {
                    labels.captured
                }
            add(
                PdfTextBlock(
                    "$timeLabel: ${labels.completedValue(item.capturedAt)}",
                    PdfTextStyle.META,
                    group = group,
                    category = key,
                ),
            )
            if (item.applicability == Applicability.NOT_APPLICABLE) {
                add(
                    PdfTextBlock(
                        labels.excluded + " (NOT_APPLICABLE)",
                        PdfTextStyle.META,
                        group = group,
                        category = key,
                    ),
                )
            }
            val bytes = (item.value as? EvidenceValue.LongValue)?.value?.takeIf { item.unit?.value == "bytes" }
            bytes?.let {
                add(
                    PdfTextBlock("${labels.numberValue(it)} B", PdfTextStyle.META, group = group, category = key),
                )
            }
            item.presentationReason()?.takeIf { shouldShowEvidenceReason(item.status, it) }?.let {
                add(
                    PdfTextBlock(
                        "${labels.reason}: ${labels.reasonName(it)}",
                        PdfTextStyle.META,
                        group = group,
                        category = key,
                    ),
                )
            }
        }
    }

    internal fun evidenceValue(
        item: DiagnosticEvidence,
        labels: PdfReportLabels,
    ): String {
        val value = item.value ?: return labels.absentValue
        if (item.checkId.value == "sim.base_network") {
            return item.networkValueText() ?: valueText(value, labels)
        }
        if (item.unit?.value == "bytes" && value is EvidenceValue.LongValue) return labels.fileSizeValue(value.value)
        if (item.unit?.value == "samples" &&
            value is EvidenceValue.IntValue
        ) {
            return labels.sampleCountValue(value.value)
        }
        return valueText(value, labels) +
            item.unit
                ?.let(labels.unitName)
                ?.takeIf(String::isNotBlank)
                ?.let { " $it" }
                .orEmpty()
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
