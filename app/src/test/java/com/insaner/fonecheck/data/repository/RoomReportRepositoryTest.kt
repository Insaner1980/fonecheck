package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.data.local.ReportDao
import com.insaner.fonecheck.data.local.ReportEntity
import com.insaner.fonecheck.data.local.ReportSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.testing.batteryReport
import com.insaner.fonecheck.testing.testReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomReportRepositoryTest {
    @Test
    fun `reports round trip through the persisted payload and repository operations`() =
        runTest {
            val dao = FakeReportDao()
            val repository = RoomReportRepository(dao)
            val base = batteryReport("first", "Alpha")
            val evidence =
                base.categories
                    .single()
                    .evidence
                    .single()
            val first =
                base.copy(
                    categories =
                        listOf(
                            base.categories.single().copy(
                                evidence =
                                    listOf(
                                        evidence.copy(status = DiagnosticStatus.WARNING),
                                        evidence.copy(status = DiagnosticStatus.FAIL),
                                    ),
                            ),
                        ),
                )
            val second = batteryReport("second", "Beta")

            repository.insert(first)
            repository.insert(second)

            val stored = requireNotNull(dao.entities["first"])
            assertEquals("full_check", stored.reportKindCode)
            assertEquals(null, stored.categoryId)
            assertEquals(first.startedAt.toEpochMilli(), stored.startedAtEpochMillis)
            assertEquals(first.completedAt.toEpochMilli(), stored.completedAtEpochMillis)
            assertEquals(1, stored.warningCount)
            assertEquals(1, stored.failureCount)
            assertEquals(ReportLoadResult.Available(first), repository.getById("first"))
            assertEquals(
                ReportComparisonLoad(
                    ReportLoadResult.Available(first),
                    ReportLoadResult.Available(second),
                ),
                repository.getForComparison("first", "second"),
            )
            assertEquals(ReportLoadResult.NotFound, repository.getById("missing"))

            repository.delete("first")
            assertEquals(ReportLoadResult.NotFound, repository.getById("first"))
            repository.deleteAll()
            assertTrue(dao.entities.isEmpty())
        }

    @Test
    fun `category report persists its one stable category id`() =
        runTest {
            val dao = FakeReportDao()
            val repository = RoomReportRepository(dao)
            val report = batteryReport("battery-only", "Alpha").copy(kind = ReportKind.CATEGORY_ONLY)

            repository.insert(report)

            assertEquals(DiagnosticCategoryId.BATTERY.stableId, dao.entities.getValue(report.stableId).categoryId)
            assertEquals(ReportLoadResult.Available(report), repository.getById(report.stableId))
        }

    @Test
    fun `stored summaries retain valid metadata and classify invalid metadata`() =
        runTest {
            val dao = FakeReportDao()
            val repository = RoomReportRepository(dao)
            val validFull = validSummary(id = "full")
            val validCategory =
                validSummary(id = "category").copy(
                    reportKindCode = "category_only",
                    categoryId = DiagnosticCategoryId.BATTERY.stableId,
                )
            val incomplete = validSummary(id = "incomplete").copy(scoreValue = null, scoreStateCode = "incomplete")
            dao.summaries.value =
                listOf(
                    validFull,
                    validCategory,
                    incomplete,
                    validSummary(id = "complete").copy(scoreStateCode = "complete"),
                    validSummary(id = "future").copy(reportSchemaVersion = 2),
                    validSummary(id = "bad-kind").copy(reportKindCode = "future_kind"),
                    validSummary(id = "full-with-category").copy(categoryId = "battery"),
                    validSummary(id = "missing-category").copy(reportKindCode = "category_only"),
                    validSummary(id = "bad-category").copy(reportKindCode = "category_only", categoryId = "future"),
                    validSummary(id = "bad-score-state").copy(scoreStateCode = "future"),
                    validSummary(id = "missing-score").copy(scoreValue = null),
                    validSummary(id = "bad-score").copy(scoreValue = 101),
                    validSummary(id = "unexpected-score").copy(scoreStateCode = "incomplete"),
                    validSummary(id = "bad-coverage").copy(coveragePercentage = 101),
                    validSummary(id = "bad-warning-count").copy(warningCount = -1),
                    validSummary(id = "bad-failure-count").copy(failureCount = -1),
                )

            val summaries = repository.observeSummaries().first().associateBy(SavedReportSummary::stableId)

            assertEquals(ReportKind.FULL_CHECK, summaries.getValue("full").kind)
            assertEquals(null, summaries.getValue("full").categoryId)
            assertEquals(ReportKind.CATEGORY_ONLY, summaries.getValue("category").kind)
            assertEquals(DiagnosticCategoryId.BATTERY, summaries.getValue("category").categoryId)
            assertEquals(null, summaries.getValue("incomplete").unavailableReason)
            assertEquals(null, summaries.getValue("complete").unavailableReason)
            assertEquals(
                ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION,
                summaries.getValue("future").unavailableReason,
            )
            listOf(
                "bad-kind",
                "full-with-category",
                "missing-category",
                "bad-category",
                "bad-score-state",
                "missing-score",
                "bad-score",
                "unexpected-score",
                "bad-coverage",
                "bad-warning-count",
                "bad-failure-count",
            ).forEach { id ->
                assertEquals(ReportReadFailure.CORRUPT_DATA, summaries.getValue(id).unavailableReason)
            }
        }

    @Test
    fun `unsupported corrupt and inconsistent stored reports are unavailable`() =
        runTest {
            val dao = FakeReportDao()
            val repository = RoomReportRepository(dao)
            repository.insert(batteryReport("saved", "Alpha"))
            val stored = dao.entities.getValue("saved")

            dao.entities["future"] = stored.copy(id = "future", reportSchemaVersion = 2)
            dao.entities["broken-json"] = stored.copy(id = "broken-json", payloadJson = "{")
            dao.entities["inconsistent"] = stored.copy(id = "inconsistent", warningCount = 1)

            assertEquals(
                ReportLoadResult.Unavailable("future", ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION),
                repository.getById("future"),
            )
            assertEquals(
                ReportLoadResult.Unavailable("broken-json", ReportReadFailure.CORRUPT_DATA),
                repository.getById("broken-json"),
            )
            assertEquals(
                ReportLoadResult.Unavailable("inconsistent", ReportReadFailure.CORRUPT_DATA),
                repository.getById("inconsistent"),
            )
        }

    @Test
    fun `insert rejects reports that cannot be represented safely`() =
        runTest {
            val repository = RoomReportRepository(FakeReportDao())
            val battery = batteryReport("battery", "Alpha")
            val category = battery.categories.single()

            assertInvalid(repository, battery.copy(stableId = " "))
            assertInvalid(repository, battery.copy(startedAt = battery.completedAt.plusSeconds(1)))
            assertInvalid(repository, battery.copy(schemaVersion = ReportSchemaVersion(2)))
            assertInvalid(repository, battery.copy(categories = listOf(category, category)))
            assertInvalid(
                repository,
                battery.copy(categories = listOf(category.copy(categoryId = DiagnosticCategoryId.CAMERA))),
            )
            assertInvalid(repository, testReport(id = "empty-category").copy(kind = ReportKind.CATEGORY_ONLY))
            assertInvalid(
                repository,
                battery.copy(kind = ReportKind.CATEGORY_ONLY, categories = listOf(category, category)),
            )
        }

    private suspend fun assertInvalid(
        repository: RoomReportRepository,
        report: com.insaner.fonecheck.domain.model.DiagnosticReport,
    ) {
        val error = runCatching { repository.insert(report) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    private fun validSummary(id: String) =
        ReportSummary(
            id = id,
            reportKindCode = "full_check",
            categoryId = null,
            completedAtEpochMillis = 1_000L,
            reportSchemaVersion = ReportSchemaVersion.CURRENT.value,
            scoreVersion = 1,
            scoreValue = 90,
            scoreStateCode = "partial",
            coveragePercentage = 75,
            warningCount = 1,
            failureCount = 0,
        )

    private class FakeReportDao : ReportDao {
        val entities = linkedMapOf<String, ReportEntity>()
        val summaries = MutableStateFlow<List<ReportSummary>>(emptyList())

        override suspend fun insert(report: ReportEntity) {
            entities[report.id] = report
        }

        override fun observeSummaries(): Flow<List<ReportSummary>> = summaries

        override suspend fun getById(id: String): ReportEntity? = entities[id]

        override suspend fun deleteById(id: String) {
            entities.remove(id)
        }

        override suspend fun deleteAll() {
            entities.clear()
        }
    }
}
