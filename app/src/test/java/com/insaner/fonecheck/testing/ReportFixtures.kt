package com.insaner.fonecheck.testing

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkResult
import java.time.Instant

private val defaultCompletedAt = Instant.parse("2026-08-08T10:01:00Z")

fun testReport(
    id: String = "saved-report",
    completedAt: Instant = defaultCompletedAt,
    startedAt: Instant = completedAt.minusSeconds(60),
    deviceModel: String = "Test",
    categories: List<DiagnosticCategoryResult> = emptyList(),
    scoreValue: Int? = 90,
    scoreState: ScoreState = ScoreState.PARTIAL,
    scoreVersion: ScoreVersion = ScoreVersion.CURRENT,
    coverage: CoverageSummary = CoverageSummary(4, 3, 1, 0, 75),
): DiagnosticReport =
    DiagnosticReport(
        id,
        ReportKind.FULL_CHECK,
        startedAt,
        completedAt,
        ReportDeviceContext("Finnvek", deviceModel, "Fonecheck", "test", "16", 36, null),
        ReportAppContext("1.0.0", 1L),
        categories,
        ScoreSummary(scoreVersion, scoreValue, scoreState),
        coverage,
        ReportSchemaVersion.CURRENT,
    )

fun batteryReport(
    id: String,
    deviceModel: String,
    reason: EvidenceReasonCode? = null,
): DiagnosticReport {
    val evidence =
        DiagnosticEvidence(
            categoryId = DiagnosticCategoryId.BATTERY,
            checkId = DiagnosticCheckId(DiagnosticCategoryId.BATTERY, "battery.level"),
            status = DiagnosticStatus.PASS,
            confidence = Confidence.HIGH,
            source = EvidenceSource.ANDROID_API,
            applicability = Applicability.APPLICABLE,
            value = EvidenceValue.IntValue(80),
            reason = reason,
            capturedAt = Instant.parse("2026-08-08T10:00:30Z"),
        )
    return testReport(
        id = id,
        deviceModel = deviceModel,
        categories =
            listOf(
                DiagnosticCategoryResult(
                    DiagnosticCategoryId.BATTERY,
                    DiagnosticStatus.PASS,
                    listOf(evidence),
                ),
            ),
        scoreValue = 92,
    )
}

fun testDeviceInfo(
    model: String = "model",
    rootArtifactDetected: Boolean = false,
): DeviceInfo =
    DeviceInfo(
        model = model,
        manufacturer = "manufacturer",
        brand = "brand",
        product = "product",
        androidVersion = "16",
        apiLevel = 36,
        securityPatch = "2026-08-01",
        buildNumber = "build",
        kernelVersion = "kernel",
        basebandVersion = "baseband",
        bootloaderVersion = "bootloader",
        widevineLevel = "L1",
        rootArtifactDetected = rootArtifactDetected,
        developerOptionsEnabled = false,
        usbDebuggingEnabled = false,
        capturedAt = Instant.parse("2026-08-07T12:00:00Z"),
    )

fun testStorageBenchmarkResult(
    writeRate: Double,
    readRate: Double,
    availableBeforeBytes: Long,
    capturedAt: Instant,
    error: StorageBenchmarkErrorCode? = null,
): StorageBenchmarkResult =
    StorageBenchmarkResult(
        writeMebibytesPerSecond = writeRate.takeIf { error == null },
        readMebibytesPerSecond = readRate.takeIf { error == null },
        bytesWritten = if (error == null) 64L * MEBIBYTE else 0L,
        bytesRead = if (error == null) 64L * MEBIBYTE else 0L,
        checksumCrc32 = if (error == null) 42L else 0L,
        durationMillis = 1_000L,
        dataSizeBytes = 64L * MEBIBYTE,
        bufferSizeBytes = 64 * 1_024,
        availableBeforeBytes = availableBeforeBytes,
        cleanupSucceeded = true,
        capturedAt = capturedAt,
        error = error,
    )

private const val MEBIBYTE = 1_048_576
