package com.insaner.fonecheck.domain.comparison

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ReportComparisonEngineTest {
    @Test
    fun stableCheckIdsClassifyAddedRemovedStatusAndAvailabilityChanges() {
        val before =
            report(
                id = "before",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.DEVICE,
                            DiagnosticStatus.WARNING,
                            evidence(DiagnosticCategoryId.DEVICE, "identity", DiagnosticStatus.PASS),
                            evidence(DiagnosticCategoryId.DEVICE, "security", DiagnosticStatus.NOT_AVAILABLE),
                            evidence(DiagnosticCategoryId.DEVICE, "removed", DiagnosticStatus.WARNING),
                        ),
                    ),
            )
        val after =
            report(
                id = "after",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.DEVICE,
                            DiagnosticStatus.FAIL,
                            evidence(DiagnosticCategoryId.DEVICE, "identity", DiagnosticStatus.WARNING),
                            evidence(DiagnosticCategoryId.DEVICE, "security", DiagnosticStatus.PASS),
                            evidence(DiagnosticCategoryId.DEVICE, "added", DiagnosticStatus.FAIL),
                        ),
                    ),
            )

        val comparison = ReportComparisonEngine.compare(before, after)
        val device = comparison.categories.single { it.categoryId == DiagnosticCategoryId.DEVICE }
        val changes = device.evidence.associate { it.checkId to it.change }

        assertEquals(DiagnosticCatalog.categories, comparison.categories.map { it.categoryId })
        assertEquals(EvidenceChange.ADDED, changes["device.added"])
        assertEquals(EvidenceChange.STATUS_CHANGED, changes["device.identity"])
        assertEquals(EvidenceChange.REMOVED, changes["device.removed"])
        assertEquals(EvidenceChange.NEWLY_AVAILABLE, changes["device.security"])
        assertEquals(
            AttentionChange.APPEARED,
            device.evidence.single { it.checkId == "device.added" }.attentionChange,
        )
        assertEquals(
            AttentionChange.RESOLVED,
            device.evidence.single { it.checkId == "device.removed" }.attentionChange,
        )
    }

    @Test
    fun missingNotAvailableNotTestedAndChangedValuesRemainDistinct() {
        val before =
            report(
                id = "before",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.BATTERY,
                            DiagnosticStatus.PASS,
                            evidence(
                                DiagnosticCategoryId.BATTERY,
                                "level",
                                DiagnosticStatus.PASS,
                                EvidenceValue.IntValue(80),
                            ),
                            evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.PASS),
                        ),
                    ),
            )
        val after =
            report(
                id = "after",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.BATTERY,
                            DiagnosticStatus.NOT_TESTED,
                            evidence(
                                DiagnosticCategoryId.BATTERY,
                                "level",
                                DiagnosticStatus.PASS,
                                EvidenceValue.IntValue(79),
                            ),
                            evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.NOT_TESTED),
                            evidence(DiagnosticCategoryId.BATTERY, "temperature", DiagnosticStatus.NOT_AVAILABLE),
                        ),
                    ),
            )

        val evidence =
            ReportComparisonEngine
                .compare(before, after)
                .categories
                .single { it.categoryId == DiagnosticCategoryId.BATTERY }
                .evidence
                .associateBy(EvidenceComparison::checkId)

        assertEquals(EvidenceChange.VALUE_CHANGED, evidence.getValue("battery.level").change)
        assertEquals(EvidenceChange.NOT_RUN, evidence.getValue("battery.health").change)
        assertEquals(EvidenceChange.ADDED, evidence.getValue("battery.temperature").change)
    }

    @Test
    fun compatibleVersionsExposeDeltasAndIncompatibleScoreVersionsDoNot() {
        val compatible =
            ReportComparisonEngine.compare(
                report(id = "before", score = 80, coverage = 75),
                report(id = "after", score = 86, coverage = 100),
            )
        assertEquals(
            ScoreComparison.Compatible(
                before = 80,
                after = 86,
                delta = 6,
                beforeState = ScoreState.PARTIAL,
                afterState = ScoreState.PARTIAL,
                version = ScoreVersion.CURRENT,
            ),
            compatible.score,
        )
        assertEquals(25, compatible.coverage.delta)

        val incompatible =
            ReportComparisonEngine.compare(
                report(id = "before", scoreVersion = ScoreVersion(1)),
                report(id = "after", scoreVersion = ScoreVersion(2)),
            )
        assertEquals(
            ScoreComparison.Incompatible(ScoreVersion(1), ScoreVersion(2)),
            incompatible.score,
        )
        assertNull(incompatible.score.deltaOrNull())
    }

    private fun category(
        id: DiagnosticCategoryId,
        status: DiagnosticStatus,
        vararg evidence: DiagnosticEvidence,
    ) = DiagnosticCategoryResult(id, status, evidence.toList())

    private fun evidence(
        categoryId: DiagnosticCategoryId,
        checkSuffix: String,
        status: DiagnosticStatus,
        value: EvidenceValue? = null,
    ) = DiagnosticEvidence(
        categoryId = categoryId,
        checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.$checkSuffix"),
        status = status,
        confidence = Confidence.HIGH,
        source = EvidenceSource.ANDROID_API,
        applicability = Applicability.APPLICABLE,
        value = value,
        capturedAt = Instant.parse("2026-08-08T10:00:00Z"),
    )

    private fun report(
        id: String,
        categories: List<DiagnosticCategoryResult> = emptyList(),
        score: Int = 80,
        coverage: Int = 75,
        scoreVersion: ScoreVersion = ScoreVersion.CURRENT,
    ) = DiagnosticReport(
        stableId = id,
        kind = ReportKind.FULL_CHECK,
        startedAt = Instant.parse("2026-08-08T10:00:00Z"),
        completedAt = Instant.parse("2026-08-08T10:01:00Z"),
        device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
        app = ReportAppContext("1.0.0", 1L),
        categories = categories,
        score = ScoreSummary(scoreVersion, score, ScoreState.PARTIAL),
        coverage = CoverageSummary(4, 3, 1, 0, coverage),
        schemaVersion = ReportSchemaVersion.CURRENT,
    )
}
