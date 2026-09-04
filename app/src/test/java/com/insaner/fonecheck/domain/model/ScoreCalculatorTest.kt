package com.insaner.fonecheck.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ScoreCalculatorTest {
    @Test
    fun `catalog has the exact canonical stable category order`() {
        assertEquals(
            "Category identity and order are persisted in reports. Before updating this contract, review " +
                "ReportSchemaVersion, ScoreVersion, old-report decoding, localization, privacy, exports, " +
                "and comparisons.",
            listOf(
                "device",
                "performance",
                "sim",
                "display",
                "audio",
                "camera",
                "sensors",
                "connectivity",
                "battery",
                "thermal",
                "storage",
                "vibration",
                "buttons",
                "biometrics",
            ),
            DiagnosticCatalog.categories.map(DiagnosticCategoryId::stableId),
        )
    }

    @Test
    fun `check IDs require their category prefix and stable codes are validated`() {
        DiagnosticCheckId(DiagnosticCategoryId.BATTERY, "battery.current_now")
        EvidenceReasonCode("permission_denied")
        EvidenceUnitCode("mah")

        assertValidationFails { DiagnosticCheckId(DiagnosticCategoryId.BATTERY, "display.current_now") }
        assertValidationFails { EvidenceReasonCode("permission-denied") }
        assertValidationFails { EvidenceUnitCode("mAh") }
    }

    @Test
    fun `double evidence must be finite`() {
        EvidenceValue.DoubleValue(12.5)

        assertValidationFails { EvidenceValue.DoubleValue(Double.NaN) }
        assertValidationFails { EvidenceValue.DoubleValue(Double.POSITIVE_INFINITY) }
        assertValidationFails { EvidenceValue.DoubleValue(Double.NEGATIVE_INFINITY) }
    }

    @Test
    fun `pass warning and fail use version one point values`() {
        assertScore(DiagnosticStatus.PASS, 100)
        assertScore(DiagnosticStatus.WARNING, 65)
        assertScore(DiagnosticStatus.FAIL, 0)
    }

    @Test
    fun `informational evidence does not increase a category score`() {
        val result =
            ScoreCalculator.calculate(
                listOf(
                    category(
                        DiagnosticCategoryId.BATTERY,
                        evidence(DiagnosticCategoryId.BATTERY, "battery.pass", DiagnosticStatus.PASS),
                        evidence(DiagnosticCategoryId.BATTERY, "battery.info", DiagnosticStatus.INFO),
                    ),
                ),
            )

        assertEquals(100, result.score.value)
    }

    @Test
    fun `category scores have equal weight regardless of evidence count`() {
        val result =
            ScoreCalculator.calculate(
                listOf(
                    category(
                        DiagnosticCategoryId.BATTERY,
                        evidence(DiagnosticCategoryId.BATTERY, "battery.pass_one", DiagnosticStatus.PASS),
                        evidence(DiagnosticCategoryId.BATTERY, "battery.pass_two", DiagnosticStatus.PASS),
                    ),
                    category(
                        DiagnosticCategoryId.CAMERA,
                        evidence(DiagnosticCategoryId.CAMERA, "camera.fail", DiagnosticStatus.FAIL),
                    ),
                ),
            )

        assertEquals(50, result.score.value)
    }

    @Test
    fun `coverage thresholds hide or expose scores correctly`() {
        val atSixtyNine = ScoreCalculator.calculate(listOf(categoryWithCoverage(DiagnosticCategoryId.BATTERY, 69)))
        val atSeventy = ScoreCalculator.calculate(listOf(categoryWithCoverage(DiagnosticCategoryId.BATTERY, 70)))
        val atOneHundred = ScoreCalculator.calculate(listOf(categoryWithCoverage(DiagnosticCategoryId.BATTERY, 100)))

        assertEquals(69, atSixtyNine.coverage.percentage)
        assertEquals(ScoreState.INCOMPLETE, atSixtyNine.score.state)
        assertNull(atSixtyNine.score.value)
        assertEquals(70, atSeventy.coverage.percentage)
        assertEquals(ScoreState.PARTIAL, atSeventy.score.state)
        assertEquals(100, atSeventy.score.value)
        assertEquals(100, atOneHundred.coverage.percentage)
        assertEquals(ScoreState.COMPLETE, atOneHundred.score.state)
        assertEquals(100, atOneHundred.score.value)
    }

    @Test
    fun `not available hardware is excluded from coverage`() {
        val result =
            ScoreCalculator.calculate(
                listOf(
                    category(
                        DiagnosticCategoryId.CAMERA,
                        evidence(DiagnosticCategoryId.CAMERA, "camera.available", DiagnosticStatus.PASS),
                        evidence(
                            DiagnosticCategoryId.CAMERA,
                            "camera.flash",
                            DiagnosticStatus.NOT_AVAILABLE,
                            EvidenceReasonCode.HARDWARE_UNAVAILABLE,
                        ),
                    ),
                ),
            )

        assertEquals(100, result.coverage.percentage)
        assertEquals(1, result.coverage.unavailableCount)
    }

    @Test
    fun `applicable not tested reasons reduce coverage`() {
        val result =
            ScoreCalculator.calculate(
                listOf(
                    category(
                        DiagnosticCategoryId.SIM,
                        evidence(DiagnosticCategoryId.SIM, "sim.available", DiagnosticStatus.PASS),
                        evidence(
                            DiagnosticCategoryId.SIM,
                            "sim.permission",
                            DiagnosticStatus.NOT_TESTED,
                            EvidenceReasonCode.PERMISSION_DENIED,
                        ),
                        evidence(
                            DiagnosticCategoryId.SIM,
                            "sim.skip",
                            DiagnosticStatus.NOT_TESTED,
                            EvidenceReasonCode.SKIPPED,
                        ),
                        evidence(
                            DiagnosticCategoryId.SIM,
                            "sim.error",
                            DiagnosticStatus.NOT_TESTED,
                            EvidenceReasonCode.ERROR,
                        ),
                    ),
                ),
            )

        assertEquals(25, result.coverage.percentage)
        assertEquals(3, result.coverage.notTestedCount)
    }

    @Test
    fun `empty and informational reports have no numeric score`() {
        val empty = ScoreCalculator.calculate(emptyList())
        val informational =
            ScoreCalculator.calculate(
                listOf(
                    category(
                        DiagnosticCategoryId.DEVICE,
                        evidence(DiagnosticCategoryId.DEVICE, "device.info", DiagnosticStatus.INFO),
                    ),
                ),
            )

        assertNull(empty.score.value)
        assertEquals(ScoreState.INCOMPLETE, empty.score.state)
        assertNull(informational.score.value)
        assertEquals(ScoreState.INCOMPLETE, informational.score.state)
        assertEquals(100, informational.coverage.percentage)
    }

    @Test
    fun `only identical score versions are compatible`() {
        assertTrue(ScoreVersion.CURRENT.isCompatibleWith(ScoreVersion.CURRENT))
        assertFalse(ScoreVersion.CURRENT.isCompatibleWith(ScoreVersion(1)))
    }

    private fun assertScore(
        status: DiagnosticStatus,
        expectedScore: Int,
    ) {
        val result =
            ScoreCalculator.calculate(
                listOf(
                    category(
                        DiagnosticCategoryId.BATTERY,
                        evidence(DiagnosticCategoryId.BATTERY, "battery.check", status),
                    ),
                ),
            )

        assertEquals(expectedScore, result.score.value)
    }

    private fun categoryWithCoverage(
        category: DiagnosticCategoryId,
        completedCount: Int,
    ): DiagnosticCategoryResult =
        category(
            category,
            *(0 until 100)
                .map { index ->
                    evidence(
                        category,
                        "${category.stableId}.check_$index",
                        if (index < completedCount) DiagnosticStatus.PASS else DiagnosticStatus.NOT_TESTED,
                    )
                }.toTypedArray(),
        )

    private fun category(
        category: DiagnosticCategoryId,
        vararg evidence: DiagnosticEvidence,
    ) = DiagnosticCategoryResult(category, aggregateStatus = DiagnosticStatus.INFO, evidence = evidence.toList())

    private fun evidence(
        category: DiagnosticCategoryId,
        checkId: String,
        status: DiagnosticStatus,
        reason: EvidenceReasonCode? = null,
    ) = DiagnosticEvidence(
        categoryId = category,
        checkId = DiagnosticCheckId(category, checkId),
        status = status,
        confidence = Confidence.HIGH,
        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
        applicability = Applicability.APPLICABLE,
        reason = reason,
        capturedAt = Instant.EPOCH,
    )

    private fun assertValidationFails(block: () -> Unit) {
        try {
            block()
        } catch (_: IllegalArgumentException) {
            return
        }
        throw AssertionError("Expected validation to fail")
    }
}
