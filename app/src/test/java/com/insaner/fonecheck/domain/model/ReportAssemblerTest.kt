package com.insaner.fonecheck.domain.model

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ReportAssemblerTest {
    @Test
    fun `full report is catalog ordered and deterministically serializable`() {
        val request =
            request(
                snapshots =
                    DiagnosticCatalog.categories
                        .reversed()
                        .map { snapshot(it, DiagnosticStatus.PASS) },
            )

        val first = ReportAssembler.assemble(request)
        val second = ReportAssembler.assemble(request)

        assertEquals(DiagnosticCatalog.categories, first.categories.map(DiagnosticCategoryResult::categoryId))
        assertEquals(first, second)
        assertEquals(ReportPayloadCodec.encode(first), ReportPayloadCodec.encode(second))
        assertEquals(ReportKind.FULL_CHECK, first.kind)
        assertEquals(ReportSchemaVersion.CURRENT, first.schemaVersion)
        assertEquals(ScoreSummary(ScoreVersion.CURRENT, 100, ScoreState.COMPLETE), first.score)
        assertEquals(
            CoverageSummary(
                applicableCount = DiagnosticCatalog.categories.size,
                completedCount = DiagnosticCatalog.categories.size,
                notTestedCount = 0,
                unavailableCount = 0,
                percentage = 100,
            ),
            first.coverage,
        )
    }

    @Test
    fun `partial report uses existing score and coverage semantics`() {
        val snapshots =
            DiagnosticCatalog.categories.mapIndexed { index, categoryId ->
                snapshot(
                    categoryId,
                    if (index < 10) DiagnosticStatus.PASS else DiagnosticStatus.NOT_TESTED,
                )
            }

        val report = ReportAssembler.assemble(request(snapshots = snapshots))

        assertEquals(ScoreSummary(ScoreVersion.CURRENT, 100, ScoreState.PARTIAL), report.score)
        assertEquals(71, report.coverage.percentage)
        assertEquals(10, report.coverage.completedCount)
        assertEquals(4, report.coverage.notTestedCount)
    }

    @Test
    fun `not tested applicable evidence prevents a category pass`() {
        val report =
            ReportAssembler.assemble(
                request(
                    kind = ReportKind.CATEGORY_ONLY,
                    snapshots =
                        listOf(
                            snapshot(
                                DiagnosticCategoryId.CAMERA,
                                DiagnosticStatus.PASS,
                                DiagnosticStatus.NOT_TESTED,
                            ),
                        ),
                ),
            )

        assertEquals(DiagnosticStatus.NOT_TESTED, report.categories.single().aggregateStatus)
        assertEquals(ScoreState.INCOMPLETE, report.score.state)
        assertNull(report.score.value)
    }

    @Test
    fun `unavailable evidence is excluded from coverage and numeric score`() {
        val snapshots =
            DiagnosticCatalog.categories.map { categoryId ->
                snapshot(categoryId, DiagnosticStatus.NOT_AVAILABLE)
            }

        val report = ReportAssembler.assemble(request(snapshots = snapshots))

        assertNull(report.score.value)
        assertEquals(ScoreState.INCOMPLETE, report.score.state)
        assertEquals(0, report.coverage.applicableCount)
        assertEquals(DiagnosticCatalog.categories.size, report.coverage.unavailableCount)
        assertEquals(
            List(DiagnosticCatalog.categories.size) { DiagnosticStatus.NOT_AVAILABLE },
            report.categories.map(DiagnosticCategoryResult::aggregateStatus),
        )
    }

    @Test
    fun `non applicable failure does not override applicable evidence`() {
        val categoryId = DiagnosticCategoryId.BATTERY
        val applicablePass = snapshot(categoryId, DiagnosticStatus.PASS).evidence.single()
        val nonApplicableFailure =
            snapshot(categoryId, DiagnosticStatus.FAIL)
                .evidence
                .single()
                .copy(
                    checkId = DiagnosticCheckId(categoryId, "battery.non_applicable_failure"),
                    applicability = Applicability.NOT_APPLICABLE,
                )

        val report =
            ReportAssembler.assemble(
                request(
                    kind = ReportKind.CATEGORY_ONLY,
                    snapshots =
                        listOf(
                            DiagnosticCategorySnapshot(
                                version = DiagnosticSnapshotVersion.CURRENT,
                                categoryId = categoryId,
                                evidence = listOf(applicablePass, nonApplicableFailure),
                            ),
                        ),
                ),
            )

        assertEquals(DiagnosticStatus.PASS, report.categories.single().aggregateStatus)
    }

    @Test
    fun `category retest contains only the requested category`() {
        val request =
            request(
                kind = ReportKind.CATEGORY_ONLY,
                snapshots = listOf(snapshot(DiagnosticCategoryId.BATTERY, DiagnosticStatus.WARNING)),
            )

        val report = ReportAssembler.assemble(request)

        assertEquals(ReportKind.CATEGORY_ONLY, report.kind)
        assertEquals(listOf(DiagnosticCategoryId.BATTERY), report.categories.map(DiagnosticCategoryResult::categoryId))
        assertEquals(DiagnosticStatus.WARNING, report.categories.single().aggregateStatus)
        assertEquals(ScoreSummary(ScoreVersion.CURRENT, 65, ScoreState.COMPLETE), report.score)
        assertEquals(100, report.coverage.percentage)
    }

    private fun request(
        kind: ReportKind = ReportKind.FULL_CHECK,
        snapshots: List<DiagnosticCategorySnapshot>,
    ) = ReportAssemblyRequest(
        stableId = "report-golden",
        kind = kind,
        startedAt = Instant.parse("2026-08-07T12:00:00Z"),
        completedAt = Instant.parse("2026-08-07T12:01:00Z"),
        device =
            ReportDeviceContext(
                manufacturer = "Finnvek",
                model = "Test Device",
                brand = "fonecheck",
                product = "golden",
                androidRelease = "16",
                apiLevel = 36,
                securityPatch = "2026-08-01",
            ),
        app = ReportAppContext(versionName = "1.0.0", versionCode = 1L),
        snapshots = snapshots,
    )

    private fun snapshot(
        categoryId: DiagnosticCategoryId,
        vararg statuses: DiagnosticStatus,
    ): DiagnosticCategorySnapshot =
        DiagnosticCategorySnapshot(
            version = DiagnosticSnapshotVersion.CURRENT,
            categoryId = categoryId,
            evidence =
                statuses.mapIndexed { index, status ->
                    DiagnosticEvidence(
                        categoryId = categoryId,
                        checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.golden_$index"),
                        status = status,
                        confidence = Confidence.HIGH,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                        applicability =
                            if (status == DiagnosticStatus.NOT_AVAILABLE) {
                                Applicability.NOT_APPLICABLE
                            } else {
                                Applicability.APPLICABLE
                            },
                        reason =
                            when (status) {
                                DiagnosticStatus.WARNING -> EvidenceReasonCode.DEGRADED
                                DiagnosticStatus.NOT_AVAILABLE -> EvidenceReasonCode.HARDWARE_UNAVAILABLE
                                DiagnosticStatus.NOT_TESTED -> EvidenceReasonCode.SKIPPED
                                else -> null
                            },
                        capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
                    )
                },
        )
}
