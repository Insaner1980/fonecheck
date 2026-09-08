package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.data.local.ReportDao
import com.insaner.fonecheck.data.local.ReportEntity
import com.insaner.fonecheck.data.local.ReportSummary
import com.insaner.fonecheck.domain.comparison.EvidenceChange
import com.insaner.fonecheck.domain.comparison.ReportComparisonEngine
import com.insaner.fonecheck.domain.comparison.ScoreComparison
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.export.PdfReportLabels
import com.insaner.fonecheck.export.PdfTextStyle
import com.insaner.fonecheck.export.ReportPdfContentBuilder
import com.insaner.fonecheck.testing.batteryReport
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresenter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalReportCompatibilityTest {
    @Test
    fun `stored historical subset loads without modifying its row`() =
        runTest {
            assertStoredReportLoads(syntheticReport(listOf(DiagnosticCategoryId.BATTERY, DiagnosticCategoryId.CAMERA)))
        }

    @Test
    fun `stored historical catalog order loads unchanged`() =
        runTest {
            assertStoredReportLoads(syntheticReport(DiagnosticCatalog.categories.reversed()))
        }

    @Test
    fun `decoded historical detail preserves scope order evidence and counts`() {
        val report = syntheticReport(listOf(DiagnosticCategoryId.CAMERA, DiagnosticCategoryId.BATTERY))
        val decoded = ReportPayloadCodec.decode(ReportPayloadCodec.encode(report))
        val detail = ReportDetailPresenter.present(decoded)

        assertEquals(report.categories, detail.categories)
        assertEquals(2, detail.counts.pass)
        assertEquals(0, detail.counts.notTested)
        assertEquals(60_000L, detail.durationMillis)
    }

    @Test
    fun `historical PDF preserves category scope order and saved score`() {
        val report = syntheticReport(listOf(DiagnosticCategoryId.CAMERA, DiagnosticCategoryId.BATTERY))
        val blocks = ReportPdfContentBuilder.build(report, PdfReportLabels.english())

        assertEquals(
            listOf("Camera — pass", "Battery — pass"),
            blocks.filter { it.style == PdfTextStyle.CATEGORY }.map { it.text },
        )
        assertTrue(blocks.any { it.text == "Score version: 1" })
        assertTrue(blocks.any { it.text == "Score: 100" })
        assertTrue(blocks.any { it.text == "Coverage: 100%" })
        report.categories.flatMap { it.evidence }.forEach { item ->
            assertTrue(blocks.any { it.text.startsWith(item.checkId.value) })
        }
    }

    @Test
    fun `historical comparison includes only stored union with absent sides left absent`() {
        val before = syntheticReport(listOf(DiagnosticCategoryId.CAMERA, DiagnosticCategoryId.BATTERY))
        val after = syntheticReport(listOf(DiagnosticCategoryId.BATTERY, DiagnosticCategoryId.AUDIO))
        val comparison = ReportComparisonEngine.compare(before, after)

        assertEquals(
            listOf(DiagnosticCategoryId.CAMERA, DiagnosticCategoryId.BATTERY, DiagnosticCategoryId.AUDIO),
            comparison.categories.map { it.categoryId },
        )
        val removed = comparison.categories.first()
        val added = comparison.categories.last()
        assertNull(removed.afterStatus)
        assertNull(removed.evidence.single().after)
        assertEquals(EvidenceChange.REMOVED, removed.evidence.single().change)
        assertNull(added.beforeStatus)
        assertNull(added.evidence.single().before)
        assertEquals(EvidenceChange.ADDED, added.evidence.single().change)
        assertEquals(before.score.value, (comparison.score as ScoreComparison.Compatible).before)
    }

    @Test
    fun `current full and category reports still insert load and present`() =
        runTest {
            val currentFull = syntheticReport(DiagnosticCatalog.categories).let {
                it.copy(score = it.score.copy(version = ScoreVersion.CURRENT))
            }
            for (report in listOf(currentFull, batteryReport("category", "Test"))) {
                val dao = StoredRowDao()
                val repository = RoomReportRepository(dao)
                repository.insert(report)
                val loaded = repository.getById(report.stableId) as ReportLoadResult.Available
                assertEquals(report, loaded.report)
                assertEquals(report.categories, ReportDetailPresenter.present(loaded.report).categories)
            }
        }

    @Test
    fun `confirming an existing historical report preserves immutable collision handling`() =
        runTest {
            val report = syntheticReport(listOf(DiagnosticCategoryId.BATTERY))
            val row = storedRow(report)
            val dao = StoredRowDao()
            dao.insert(row)
            val repository = RoomReportRepository(dao)

            assertTrue(repository.insertOrConfirm(report))
            assertFalse(repository.insertOrConfirm(report.copy(app = report.app.copy(versionCode = 2))))
            assertEquals(row, dao.getById(report.stableId))
        }

    @Test
    fun `historical read compatibility does not allow incomplete or unordered new full checks`() =
        runTest {
            for (categories in listOf(listOf(DiagnosticCategoryId.BATTERY), DiagnosticCatalog.categories.reversed())) {
                val dao = StoredRowDao()
                val repository = RoomReportRepository(dao)
                val report = syntheticReport(categories)
                assertTrue(runCatching { repository.insert(report) }.exceptionOrNull() is IllegalArgumentException)
                assertFalse(repository.insertOrConfirm(report))
                assertTrue(dao.rows.isEmpty())
            }
        }

    @Test
    fun `supported stored payloads still reject invalid identities scope and relationships`() =
        runTest {
            val base = syntheticReport(listOf(DiagnosticCategoryId.BATTERY, DiagnosticCategoryId.CAMERA))
            val battery = base.categories.first()
            val invalid = listOf(
                base.copy(categories = emptyList()),
                base.copy(categories = listOf(battery, battery)),
                base.copy(categories = listOf(battery.copy(evidence = emptyList()))),
                base.copy(categories = listOf(battery.copy(evidence = battery.evidence + battery.evidence))),
                base.copy(categories = listOf(battery.copy(evidence = base.categories.last().evidence))),
                base.copy(kind = ReportKind.CATEGORY_ONLY),
                base.copy(schemaVersion = ReportSchemaVersion(2)),
            )
            for (report in invalid) {
                assertCorrupt(storedRow(base).copy(payloadJson = ReportPayloadCodec.encode(report)))
            }
            val payload = ReportPayloadCodec.encode(base)
            for (invalidPayload in listOf(
                "{",
                payload.replace("\"battery\"", "\"unknown_category\""),
                payload.replace("battery.fixture", "camera.fixture"),
                payload.replace("\"pass\"", "\"unknown_status\""),
                payload.replace("\"full_check\"", "\"unknown_kind\""),
            )) {
                assertCorrupt(storedRow(base).copy(payloadJson = invalidPayload))
            }
        }

    @Test
    fun `stored summary disagreements and unsupported schema remain unavailable`() =
        runTest {
            val row = storedRow(syntheticReport(listOf(DiagnosticCategoryId.BATTERY)))
            for (changed in listOf(
                row.copy(id = "different"),
                row.copy(reportKindCode = "category_only", categoryId = "battery"),
                row.copy(startedAtEpochMillis = row.startedAtEpochMillis - 1),
                row.copy(completedAtEpochMillis = row.completedAtEpochMillis + 1),
                row.copy(scoreVersion = 2),
                row.copy(scoreValue = 99),
                row.copy(scoreStateCode = "partial"),
                row.copy(coveragePercentage = 99),
                row.copy(applicableCount = 2, completedCount = 2),
                row.copy(completedCount = 0, notTestedCount = 1),
                row.copy(unavailableCount = 1),
                row.copy(warningCount = 1),
                row.copy(failureCount = 1),
            )) {
                assertCorrupt(changed)
            }
            val future = row.copy(reportSchemaVersion = 2)
            val dao = StoredRowDao()
            dao.insert(future)
            assertEquals(
                ReportLoadResult.Unavailable(row.id, ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION),
                RoomReportRepository(dao).getById(row.id),
            )
            assertEquals(future, dao.getById(row.id))
        }

    private suspend fun assertStoredReportLoads(report: DiagnosticReport) {
        val dao = StoredRowDao()
        val row = storedRow(report)
        // Bypass current insertion rules: this is an already stored, supported snapshot.
        dao.insert(row)
        val repository = RoomReportRepository(dao)
        val loaded = repository.getById(report.stableId)
        assertEquals(ReportLoadResult.Available(report), loaded)
        val saved = (loaded as ReportLoadResult.Available).report
        ReportDetailPresenter.present(saved)
        ReportPdfContentBuilder.build(saved, PdfReportLabels.english())
        // JSON export uses this codec; compare diagnostic meaning, not serialized bytes.
        assertEquals(report, ReportPayloadCodec.decode(ReportPayloadCodec.encode(saved)))
        assertEquals(row, dao.getById(report.stableId))
    }

    private suspend fun assertCorrupt(row: ReportEntity) {
        val dao = StoredRowDao()
        dao.insert(row)
        assertEquals(
            ReportLoadResult.Unavailable(row.id, ReportReadFailure.CORRUPT_DATA),
            RoomReportRepository(dao).getById(row.id),
        )
        assertEquals(row, dao.getById(row.id))
    }

    // Synthetic compatibility snapshots, not records attributed to a released app version.
    private fun syntheticReport(ids: List<DiagnosticCategoryId>): DiagnosticReport {
        val base = batteryReport("synthetic-history", "Test")
        val template = base.categories.single().evidence.single()
        return base.copy(
            kind = ReportKind.FULL_CHECK,
            categories =
                ids.map { id ->
                    base.categories.single().copy(
                        categoryId = id,
                        aggregateStatus = DiagnosticStatus.PASS,
                        evidence =
                            listOf(
                                template.copy(
                                    categoryId = id,
                                    checkId = DiagnosticCheckId(id, "${id.stableId}.fixture"),
                                    status = DiagnosticStatus.PASS,
                                ),
                            ),
                    )
                },
            score = ScoreSummary(ScoreVersion(1), 100, ScoreState.COMPLETE),
            coverage = base.coverage.copy(applicableCount = ids.size, completedCount = ids.size),
        )
    }

    private fun storedRow(report: DiagnosticReport) = ReportEntity(
        id = report.stableId,
        reportKindCode = report.kind.stableCode(),
        categoryId = null,
        startedAtEpochMillis = report.startedAt.toEpochMilli(),
        completedAtEpochMillis = report.completedAt.toEpochMilli(),
        reportSchemaVersion = report.schemaVersion.value,
        scoreVersion = report.score.version.value,
        scoreValue = report.score.value,
        scoreStateCode = report.score.state.stableCode(),
        coveragePercentage = report.coverage.percentage,
        applicableCount = report.coverage.applicableCount,
        completedCount = report.coverage.completedCount,
        notTestedCount = report.coverage.notTestedCount,
        unavailableCount = report.coverage.unavailableCount,
        warningCount = 0,
        failureCount = 0,
        payloadJson = ReportPayloadCodec.encode(report),
    )

    private class StoredRowDao : ReportDao {
        val rows = mutableMapOf<String, ReportEntity>()

        override suspend fun insert(report: ReportEntity) {
            check(report.id !in rows)
            rows[report.id] = report
        }

        override suspend fun getById(id: String) = rows[id]

        override fun observeSummaries() = flowOf(emptyList<ReportSummary>())

        override suspend fun deleteById(id: String) {
            error("Reading must not delete stored reports")
        }

        override suspend fun deleteAll() {
            error("Reading must not delete stored reports")
        }
    }
}
