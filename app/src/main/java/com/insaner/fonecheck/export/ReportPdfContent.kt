package com.insaner.fonecheck.export

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
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
import com.insaner.fonecheck.domain.model.networkPresentation
import com.insaner.fonecheck.domain.model.networkValueText
import com.insaner.fonecheck.domain.model.presentationConfidence
import com.insaner.fonecheck.domain.model.presentationReason
import com.insaner.fonecheck.localization.shouldShowEvidenceReason
import com.insaner.fonecheck.ui.format.evidenceFractionDigits
import java.math.BigDecimal
import java.math.RoundingMode
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

/** A verdict drawn as a shape as well as a colour, so it survives a greyscale print. */
enum class PdfMark {
    PASS,
    FAIL,
    WARNING,
    PARTIAL,
    NOT_MEASURED,
    NOT_AVAILABLE,
}

/** Informational observations carry no mark: they are descriptions, not verdicts. */
fun DiagnosticStatus.pdfMark(): PdfMark? =
    when (this) {
        DiagnosticStatus.PASS -> PdfMark.PASS
        DiagnosticStatus.FAIL -> PdfMark.FAIL
        DiagnosticStatus.WARNING -> PdfMark.WARNING
        DiagnosticStatus.NOT_TESTED -> PdfMark.NOT_MEASURED
        DiagnosticStatus.NOT_AVAILABLE -> PdfMark.NOT_AVAILABLE
        DiagnosticStatus.INFO -> null
    }

/** Joins short facts that share one line. U+00B7 is banned as a separator (UiSeparatorPolicyTest). */
internal const val PDF_SEPARATOR = ", "

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
    /** Style of each text, label first; a missing entry uses [style]. */
    val columnStyles: List<PdfTextStyle> = emptyList(),
    /** Drawn before the last text. In a table row it also tints that status text. */
    val mark: PdfMark? = null,
    /** Mark and label pairs set in a line: the status legend. */
    val legend: List<Pair<PdfMark, String>> = emptyList(),
) {
    val allText: String get() = (listOf(text) + columns + legend.map { it.second }).joinToString(" ")
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
    val interpretation: String =
        "Pass means a recorded criterion was met; fail means it was not; warning marks a recorded concern. " +
            "Info is descriptive. " +
            "Not measured has no completed result; not available is excluded. " +
            "Source describes how an observation was obtained; confidence describes its reliability. " +
            "The report ID identifies a report, not an authenticated handset. " +
            "This file is not signed or independently verified.",
    val confidenceNote: String = "Confidence is shown only where it is not high.",
    val continued: String = "Continued",
    val partial: String = "Partial",
    val testsGroup: String = "Hardware tests",
    val infoGroup: String = "Device information",
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
    val coverage: String,
    val categories: String,
    val source: String,
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
    /** A person's answer to a manual check, which "yes" or "no" would not make readable. */
    val userConfirmedValue: (Boolean) -> String = { if (it) "confirmed working" else "problem reported" },
    val numberValue: (Number) -> String,
    val fixedNumberValue: (Number, Int) -> String = { value, digits ->
        BigDecimal(value.toString()).setScale(digits, RoundingMode.HALF_EVEN).toPlainString()
    },
    val percentValue: (String) -> String = { "$it%" },
    val unitName: (EvidenceUnitCode) -> String,
    val completedValue: (Instant) -> String,
    /** An observation time, shortened when the report header already states its date and offset. */
    val observedAtValue: (observed: Instant, completed: Instant) -> String = { observed, _ -> observed.toString() },
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
                coverage = "Coverage",
                categories = "Diagnostic categories",
                source = "Source",
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

/** The observations a category's coverage is counted over. */
private fun DiagnosticCategoryResult.applicableEvidence(): List<DiagnosticEvidence> =
    evidence.filter {
        !it.isNetworkMetadata && it.applicability == Applicability.APPLICABLE &&
            it.status != DiagnosticStatus.NOT_AVAILABLE
    }

fun pdfCategoryCounts(category: DiagnosticCategoryResult): PdfCategoryCounts {
    val applicable = category.applicableEvidence()
    return PdfCategoryCounts(
        applicable.count { it.status != DiagnosticStatus.NOT_TESTED },
        applicable.size,
        applicable.count { it.status == DiagnosticStatus.NOT_TESTED },
    )
}

/** A category that measured some of its observations but not all is partial, not unmeasured. */
private fun pdfCategoryMark(
    category: DiagnosticCategoryResult,
    counts: PdfCategoryCounts,
): PdfMark? =
    if (category.aggregateStatus == DiagnosticStatus.NOT_TESTED && counts.completed > 0) {
        PdfMark.PARTIAL
    } else {
        category.aggregateStatus.pdfMark()
    }

/** Categories that describe the phone rather than test a part of it. */
private val DEVICE_INFORMATION_CATEGORIES =
    setOf(
        DiagnosticCategoryId.DEVICE,
        DiagnosticCategoryId.PERFORMANCE,
        DiagnosticCategoryId.SIM,
        DiagnosticCategoryId.STORAGE,
    )

private data class PdfCategoryGroup(
    val name: String,
    val categories: List<DiagnosticCategoryResult>,
)

/** Hardware tests before device information; the saved order holds within each group. */
private fun categoryGroups(
    categories: List<DiagnosticCategoryResult>,
    labels: PdfReportLabels,
): List<PdfCategoryGroup> {
    val (information, tests) = categories.partition { it.categoryId in DEVICE_INFORMATION_CATEGORIES }
    return listOf(PdfCategoryGroup(labels.testsGroup, tests), PdfCategoryGroup(labels.infoGroup, information))
        .filter { it.categories.isNotEmpty() }
}

object ReportPdfContentBuilder {
    fun build(
        report: DiagnosticReport,
        labels: PdfReportLabels,
    ): List<PdfTextBlock> =
        buildList {
            val groups = categoryGroups(report.categories, labels)
            addSummary(report, labels)
            addFindings(report, groups, labels)
            addCategoryTable(groups, labels)
            groups.forEach { group ->
                add(PdfTextBlock("${labels.details}: ${group.name}", PdfTextStyle.HEADING, keepWithNext = true))
                group.categories.forEach { category -> addCategory(category, labels, report.completedAt) }
            }
            addNotes(report, labels)
        }

    private fun MutableList<PdfTextBlock>.line(
        text: String,
        style: PdfTextStyle = PdfTextStyle.BODY,
        finding: Boolean = false,
        mark: PdfMark? = null,
    ) = add(PdfTextBlock(text, style, finding = finding, mark = mark))

    private fun MutableList<PdfTextBlock>.addSummary(
        report: DiagnosticReport,
        labels: PdfReportLabels,
    ) {
        val number = labels.numberValue
        val statuses = report.categories.flatMap { it.evidence }.map { it.status }
        line(labels.title, PdfTextStyle.TITLE)
        line(
            listOfNotNull(
                "${labels.device}: ${report.device.manufacturer} ${report.device.model}",
                "${labels.android}: ${report.device.androidRelease} (API ${number(report.device.apiLevel)})",
                report.device.securityPatch?.let { "${labels.securityPatch}: $it" },
            ).joinToString(PDF_SEPARATOR),
        )
        val duration = Duration.between(report.startedAt, report.completedAt).coerceAtLeast(Duration.ZERO)
        // A localized date carries its own commas, so the duration takes a line of its own.
        line("${labels.completed}: ${labels.completedValue(report.completedAt)}", PdfTextStyle.META)
        line("${labels.duration}: ${labels.durationValue(duration)}", PdfTextStyle.META)
        line(labels.scope(report), PdfTextStyle.META)
        val scoreValue = report.score.value?.let { "${number(it)} / ${number(100)}" } ?: labels.absentValue
        line("${labels.score}: $scoreValue", PdfTextStyle.HEADING)
        line(labels.scoreStateName(report.score.state))
        line(
            "${labels.coverage}: ${labels.percentValue(number(report.coverage.percentage))}" +
                "$PDF_SEPARATOR${labels.completedApplicable}: " +
                "${number(report.coverage.completedCount)}/${number(report.coverage.applicableCount)}",
        )
        line(
            listOf(
                "${labels.failures}: ${number(statuses.count { it == DiagnosticStatus.FAIL })}",
                "${labels.warnings}: ${number(statuses.count { it == DiagnosticStatus.WARNING })}",
                "${labels.notMeasured}: ${number(report.coverage.notTestedCount)}",
                "${labels.excluded}: ${number(report.coverage.unavailableCount)}",
            ).joinToString(PDF_SEPARATOR),
        )
        // The two figures above are easy to confuse; say what each counts before any finding.
        line(labels.scoreScopeNote, PdfTextStyle.META)
    }

    private fun MutableList<PdfTextBlock>.addFindings(
        report: DiagnosticReport,
        groups: List<PdfCategoryGroup>,
        labels: PdfReportLabels,
    ) {
        val evidence = report.categories.flatMap { it.evidence }
        val failures = evidence.filter { it.status == DiagnosticStatus.FAIL }
        val warnings = evidence.filter { it.status == DiagnosticStatus.WARNING }
        add(PdfTextBlock(labels.findings, PdfTextStyle.HEADING, keepWithNext = true))
        if (failures.isEmpty()) line(labels.noFailures)
        // The measured layout bounds this selection as well as this deterministic item limit.
        (failures + warnings).take(3).forEach { item ->
            val reason = item.presentationReason()?.let(labels.reasonName)
            line(
                "${labels.statusName(item.status)}: ${labels.checkName(item)} " +
                    "(${labels.categoryName(item.categoryId)}). " +
                    "${labels.source}: ${labels.sourceName(item.source)}." + reason?.let { " $it" }.orEmpty(),
                finding = true,
                mark = item.status.pdfMark(),
            )
        }
        // Name what was not measured; a bare count leaves the reader guessing which parts were skipped.
        groups.flatMap { it.categories }.forEach { category ->
            val missing = category.applicableEvidence().filter { it.status == DiagnosticStatus.NOT_TESTED }
            if (missing.isNotEmpty()) {
                line(
                    "${labels.categoryName(category.categoryId)} — ${labels.notMeasured}: " +
                        missing.joinToString(", ") { labels.checkName(it) },
                    PdfTextStyle.META,
                    finding = true,
                    mark = PdfMark.NOT_MEASURED,
                )
            }
        }
        line(labels.findingsReference, PdfTextStyle.META)
        line(labels.disclaimer, PdfTextStyle.META)
    }

    /**
     * Each group stays whole under its own header row, so a page break can fall only between groups:
     * the device information then continues on the next page still labelled.
     */
    private fun MutableList<PdfTextBlock>.addCategoryTable(
        groups: List<PdfCategoryGroup>,
        labels: PdfReportLabels,
    ) {
        add(PdfTextBlock(labels.categories, PdfTextStyle.HEADING, keepWithNext = true))
        groups.forEach { group ->
            val rows =
                listOf(
                    PdfTextBlock(
                        group.name,
                        PdfTextStyle.META,
                        columns = listOf(labels.completedApplicable, labels.status),
                    ),
                ) +
                    group.categories.map { category ->
                        val counts = pdfCategoryCounts(category)
                        val mark = pdfCategoryMark(category, counts)
                        PdfTextBlock(
                            labels.categoryName(category.categoryId),
                            PdfTextStyle.BODY,
                            columns =
                                listOf(
                                    "${labels.numberValue(counts.completed)}/${labels.numberValue(counts.applicable)}",
                                    categoryStatusName(category, mark, labels),
                                ),
                            columnStyles = listOf(PdfTextStyle.BODY, PdfTextStyle.MONO),
                            mark = mark,
                        )
                    }
            rows.forEachIndexed { index, row -> add(row.copy(keepWithNext = index < rows.lastIndex)) }
        }
    }

    private fun categoryStatusName(
        category: DiagnosticCategoryResult,
        mark: PdfMark?,
        labels: PdfReportLabels,
    ): String = if (mark == PdfMark.PARTIAL) labels.partial else labels.statusName(category.aggregateStatus)

    private fun MutableList<PdfTextBlock>.addNotes(
        report: DiagnosticReport,
        labels: PdfReportLabels,
    ) {
        val number = labels.numberValue
        add(PdfTextBlock(labels.notes, PdfTextStyle.HEADING, keepWithNext = true, endsCategories = true))
        add(
            PdfTextBlock(
                "",
                PdfTextStyle.META,
                legend =
                    listOf(
                        PdfMark.PASS to labels.statusName(DiagnosticStatus.PASS),
                        PdfMark.FAIL to labels.statusName(DiagnosticStatus.FAIL),
                        PdfMark.WARNING to labels.statusName(DiagnosticStatus.WARNING),
                        PdfMark.PARTIAL to labels.partial,
                        PdfMark.NOT_MEASURED to labels.statusName(DiagnosticStatus.NOT_TESTED),
                        PdfMark.NOT_AVAILABLE to labels.statusName(DiagnosticStatus.NOT_AVAILABLE),
                    ),
            ),
        )
        line(labels.interpretation, PdfTextStyle.META)
        line(labels.confidenceNote, PdfTextStyle.META)
        line(labels.timeSemantics, PdfTextStyle.META)
        line("${labels.reportId}: ${report.stableId}", PdfTextStyle.META)
        line(
            "${labels.reportFormat}: ${number(report.schemaVersion.value)}" +
                "$PDF_SEPARATOR${labels.scoreVersion}: ${number(report.score.version.value)}" +
                "$PDF_SEPARATOR${labels.app}: ${report.app.versionName} (${number(report.app.versionCode)})",
            PdfTextStyle.META,
        )
    }

    private fun MutableList<PdfTextBlock>.addCategory(
        category: DiagnosticCategoryResult,
        labels: PdfReportLabels,
        completedAt: Instant,
    ) {
        val counts = pdfCategoryCounts(category)
        val key = category.categoryId.name
        val mark = pdfCategoryMark(category, counts)
        add(
            PdfTextBlock(
                labels.categoryName(category.categoryId),
                PdfTextStyle.CATEGORY,
                columns =
                    listOf(
                        "${labels.completedApplicable}: " +
                            "${labels.numberValue(counts.completed)}/${labels.numberValue(counts.applicable)}",
                        categoryStatusName(category, mark, labels),
                    ),
                columnStyles = listOf(PdfTextStyle.CATEGORY, PdfTextStyle.META, PdfTextStyle.BODY),
                mark = mark,
                category = key,
                startsCategory = true,
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
        // Raw network callback fields stay in JSON and comparison; paper shows the two scoped readings.
        val presented = category.evidence.networkPresentation()
        if (presented.isEmpty()) {
            add(PdfTextBlock(labels.emptyEvidence, PdfTextStyle.BODY))
        }
        presented.forEachIndexed { index, item ->
            val group = "$key/$index"

            fun detail(text: String) = add(PdfTextBlock(text, PdfTextStyle.META, group = group, category = key))
            val itemMark = item.status.pdfMark()
            // An informational row leaves the status empty, so the verdicts stand out.
            val statusText = itemMark?.let { labels.statusName(item.status) }.orEmpty()
            add(
                PdfTextBlock(
                    labels.checkName(item),
                    PdfTextStyle.BODY,
                    columns = listOf(evidenceValue(item, labels), statusText),
                    columnStyles =
                        listOf(
                            PdfTextStyle.BODY,
                            if (item.value.isNumeric()) PdfTextStyle.MONO else PdfTextStyle.BODY,
                        ),
                    mark = itemMark,
                    group = group,
                    category = key,
                ),
            )
            detail(observationDetail(item, labels, completedAt))
            if (item.applicability == Applicability.NOT_APPLICABLE) detail(labels.excluded)
            item.presentationReason()?.takeIf { shouldShowEvidenceReason(item.status, it) }?.let {
                detail("${labels.reason}: ${labels.reasonName(it)}")
            }
        }
    }

    /**
     * Source, then confidence only where it is not high, then the time. A measurement that never ran,
     * or that Android does not expose, has neither a confidence nor a time worth printing.
     */
    private fun observationDetail(
        item: DiagnosticEvidence,
        labels: PdfReportLabels,
        completedAt: Instant,
    ): String {
        val measured =
            item.status != DiagnosticStatus.NOT_TESTED &&
                !(item.status == DiagnosticStatus.NOT_AVAILABLE && item.value == null)
        val confidence = item.presentationConfidence().takeIf { measured && it != Confidence.HIGH }
        val timeLabel =
            if (item.categoryId == DiagnosticCategoryId.THERMAL ||
                (item.isNetworkMetadata && item.value != null)
            ) {
                labels.readAt
            } else {
                labels.captured
            }
        val time = if (measured) "$timeLabel: ${labels.observedAtValue(item.capturedAt, completedAt)}" else null
        return listOfNotNull(labels.sourceName(item.source), confidence?.let(labels.confidenceName), time)
            .joinToString(PDF_SEPARATOR)
    }

    private fun EvidenceValue?.isNumeric(): Boolean =
        this is EvidenceValue.IntValue || this is EvidenceValue.LongValue ||
            this is EvidenceValue.DecimalValue || this is EvidenceValue.DoubleValue

    internal fun evidenceValue(
        item: DiagnosticEvidence,
        labels: PdfReportLabels,
    ): String {
        val value = item.value ?: return labels.absentValue
        if (item.checkId.value == "sim.base_network") {
            return item.networkValueText() ?: valueText(value, labels)
        }
        if (item.source == EvidenceSource.USER_CONFIRMATION && value is EvidenceValue.BooleanValue) {
            return labels.userConfirmedValue(value.value)
        }
        val unit = item.unit?.value
        if (unit == "bytes" && value is EvidenceValue.LongValue) return labels.fileSizeValue(value.value)
        if (unit == "samples" && value is EvidenceValue.IntValue) return labels.sampleCountValue(value.value)
        val digits = evidenceFractionDigits(unit)
        val number =
            if (digits != null && value is EvidenceValue.DoubleValue) {
                labels.fixedNumberValue(value.value, digits)
            } else {
                valueText(value, labels)
            }
        if (unit == "percent" && value.isNumeric()) return labels.percentValue(number)
        return number +
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
        "pixels" -> "px"
        "samples" -> "samples"
        else -> unit.value.replace('_', ' ')
    }
