package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.Instant

internal object ReportPayloadCodec {
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    fun encode(report: DiagnosticReport): String =
        json.encodeToString(StoredReportPayload.serializer(), report.toStoredPayload())

    fun decode(payloadJson: String): DiagnosticReport =
        json.decodeFromString(StoredReportPayload.serializer(), payloadJson).toDomain()
}

internal fun Enum<*>.stableCode(): String = name.lowercase()

internal inline fun <reified T : Enum<T>> enumFromStableCodeOrNull(code: String): T? =
    enumValues<T>().firstOrNull { it.stableCode() == code }

private inline fun <reified T : Enum<T>> enumFromStableCode(code: String): T =
    requireNotNull(enumFromStableCodeOrNull<T>(code)) { "Unsupported stable code: $code" }

@Serializable
private data class StoredReportPayload(
    val schemaVersion: Int,
    val stableId: String,
    val kind: String,
    val startedAt: String,
    val completedAt: String,
    val device: StoredDeviceContext,
    val app: StoredAppContext,
    val categories: List<StoredCategoryResult>,
    val score: StoredScoreSummary,
    val coverage: StoredCoverageSummary,
)

@Serializable
private data class StoredDeviceContext(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val product: String,
    val androidRelease: String,
    val apiLevel: Int,
    val securityPatch: String?,
)

@Serializable
private data class StoredAppContext(
    val versionName: String,
    val versionCode: Long,
)

@Serializable
private data class StoredCategoryResult(
    val categoryId: String,
    val aggregateStatus: String,
    val evidence: List<StoredEvidence>,
)

@Serializable
private data class StoredEvidence(
    val categoryId: String,
    val checkId: String,
    val status: String,
    val confidence: String,
    val source: String,
    val applicability: String,
    val reason: String?,
    val value: StoredEvidenceValue?,
    val unit: String?,
    val capturedAt: String,
)

@Serializable
private data class StoredEvidenceValue(
    val type: String,
    val value: String,
)

@Serializable
private data class StoredScoreSummary(
    val version: Int,
    val value: Int?,
    val state: String,
)

@Serializable
private data class StoredCoverageSummary(
    val applicableCount: Int,
    val completedCount: Int,
    val notTestedCount: Int,
    val unavailableCount: Int,
    val percentage: Int,
)

private fun DiagnosticReport.toStoredPayload() =
    StoredReportPayload(
        schemaVersion = schemaVersion.value,
        stableId = stableId,
        kind = kind.stableCode(),
        startedAt = startedAt.toString(),
        completedAt = completedAt.toString(),
        device = device.toStored(),
        app = app.toStored(),
        categories = categories.map(DiagnosticCategoryResult::toStored),
        score = score.toStored(),
        coverage = coverage.toStored(),
    )

private fun ReportDeviceContext.toStored() =
    StoredDeviceContext(
        manufacturer = manufacturer,
        model = model,
        brand = brand,
        product = product,
        androidRelease = androidRelease,
        apiLevel = apiLevel,
        securityPatch = securityPatch,
    )

private fun ReportAppContext.toStored() =
    StoredAppContext(
        versionName = versionName,
        versionCode = versionCode,
    )

private fun DiagnosticCategoryResult.toStored() =
    StoredCategoryResult(
        categoryId = categoryId.stableId,
        aggregateStatus = aggregateStatus.stableCode(),
        evidence = evidence.map(DiagnosticEvidence::toStored),
    )

private fun DiagnosticEvidence.toStored() =
    StoredEvidence(
        categoryId = categoryId.stableId,
        checkId = checkId.value,
        status = status.stableCode(),
        confidence = confidence.stableCode(),
        source = source.stableCode(),
        applicability = applicability.stableCode(),
        reason = reason?.value,
        value = value?.toStored(),
        unit = unit?.value,
        capturedAt = capturedAt.toString(),
    )

private fun EvidenceValue.toStored(): StoredEvidenceValue =
    when (this) {
        is EvidenceValue.BooleanValue -> StoredEvidenceValue("boolean", value.toString())
        is EvidenceValue.IntValue -> StoredEvidenceValue("int", value.toString())
        is EvidenceValue.LongValue -> StoredEvidenceValue("long", value.toString())
        is EvidenceValue.DecimalValue -> StoredEvidenceValue("decimal", value.toString())
        is EvidenceValue.DoubleValue -> StoredEvidenceValue("double", value.toString())
        is EvidenceValue.RawTextValue -> StoredEvidenceValue("raw_text", value)
        is EvidenceValue.StableTextCodeValue -> StoredEvidenceValue("stable_text_code", value)
    }

private fun ScoreSummary.toStored() =
    StoredScoreSummary(
        version = version.value,
        value = value,
        state = state.stableCode(),
    )

private fun CoverageSummary.toStored() =
    StoredCoverageSummary(
        applicableCount = applicableCount,
        completedCount = completedCount,
        notTestedCount = notTestedCount,
        unavailableCount = unavailableCount,
        percentage = percentage,
    )

private fun StoredReportPayload.toDomain() =
    DiagnosticReport(
        stableId = stableId,
        kind = enumFromStableCode<ReportKind>(kind),
        startedAt = Instant.parse(startedAt),
        completedAt = Instant.parse(completedAt),
        device = device.toDomain(),
        app = app.toDomain(),
        categories = categories.map(StoredCategoryResult::toDomain),
        score = score.toDomain(),
        coverage = coverage.toDomain(),
        schemaVersion = ReportSchemaVersion(schemaVersion),
    )

private fun StoredDeviceContext.toDomain() =
    ReportDeviceContext(
        manufacturer = manufacturer,
        model = model,
        brand = brand,
        product = product,
        androidRelease = androidRelease,
        apiLevel = apiLevel,
        securityPatch = securityPatch,
    )

private fun StoredAppContext.toDomain() =
    ReportAppContext(
        versionName = versionName,
        versionCode = versionCode,
    )

private fun StoredCategoryResult.toDomain() =
    DiagnosticCategoryResult(
        categoryId = categoryId.toCategoryId(),
        aggregateStatus = enumFromStableCode<DiagnosticStatus>(aggregateStatus),
        evidence = evidence.map(StoredEvidence::toDomain),
    )

private fun StoredEvidence.toDomain(): DiagnosticEvidence {
    val domainCategoryId = categoryId.toCategoryId()
    return DiagnosticEvidence(
        categoryId = domainCategoryId,
        checkId = DiagnosticCheckId(domainCategoryId, checkId),
        status = enumFromStableCode<DiagnosticStatus>(status),
        confidence = enumFromStableCode<Confidence>(confidence),
        source = enumFromStableCode<EvidenceSource>(source),
        applicability = enumFromStableCode<Applicability>(applicability),
        reason = reason?.let(::EvidenceReasonCode),
        value = value?.toDomain(),
        unit = unit?.let(::EvidenceUnitCode),
        capturedAt = Instant.parse(capturedAt),
    )
}

private fun StoredEvidenceValue.toDomain(): EvidenceValue =
    when (type) {
        "boolean" -> EvidenceValue.BooleanValue(value.toBooleanStrict())
        "int" -> EvidenceValue.IntValue(value.toInt())
        "long" -> EvidenceValue.LongValue(value.toLong())
        "decimal" -> EvidenceValue.DecimalValue(BigDecimal(value))
        "double" -> EvidenceValue.DoubleValue(value.toDouble())
        "raw_text" -> EvidenceValue.RawTextValue(value)
        "stable_text_code" -> EvidenceValue.StableTextCodeValue(value)
        else -> throw IllegalArgumentException("Unsupported evidence value type: $type")
    }

private fun StoredScoreSummary.toDomain() =
    ScoreSummary(
        version = ScoreVersion(version),
        value = value,
        state = enumFromStableCode<ScoreState>(state),
    )

private fun StoredCoverageSummary.toDomain() =
    CoverageSummary(
        applicableCount = applicableCount,
        completedCount = completedCount,
        notTestedCount = notTestedCount,
        unavailableCount = unavailableCount,
        percentage = percentage,
    )

private fun String.toCategoryId(): DiagnosticCategoryId =
    requireNotNull(DiagnosticCategoryId.entries.firstOrNull { it.stableId == this }) {
        "Unsupported diagnostic category ID: $this"
    }
