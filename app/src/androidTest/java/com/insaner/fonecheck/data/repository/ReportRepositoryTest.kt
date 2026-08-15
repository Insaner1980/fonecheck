package com.insaner.fonecheck.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.data.local.FonecheckDatabase
import com.insaner.fonecheck.data.local.ReportEntity
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ReportRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: FonecheckDatabase
    private lateinit var repository: ReportRepository
    private val databaseName = "report-repository-${UUID.randomUUID()}.db"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(databaseName)
        database = openDatabase()
        repository = RoomReportRepository(database.reportDao())
    }

    @After
    fun tearDown() {
        if (::database.isInitialized && database.isOpen) {
            database.close()
        }
        if (::context.isInitialized) {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun insertRoundTripsEveryValueTypeAfterDatabaseReopen() =
        runBlocking {
            val expected = report(id = "round-trip", completedAt = Instant.parse("2026-08-07T12:34:56.123456789Z"))

            repository.insert(expected)
            database.close()
            database = openDatabase()
            repository = RoomReportRepository(database.reportDao())

            assertEquals(ReportLoadResult.Available(expected), repository.getById(expected.stableId))
        }

    @Test
    fun summariesAreNewestFirstWithoutDecodingPayloads() =
        runBlocking {
            repository.insert(report(id = "older", completedAt = Instant.ofEpochMilli(2_000L)))
            database.reportDao().insert(storedEntity(id = "corrupt", completedAt = 2_500L, payloadJson = "{broken"))
            repository.insert(
                report(
                    id = "newer",
                    completedAt = Instant.ofEpochMilli(3_000L),
                    kind = ReportKind.CATEGORY_ONLY,
                ),
            )

            val summaries = repository.observeSummaries().first()

            assertEquals(listOf("newer", "corrupt", "older"), summaries.map(SavedReportSummary::stableId))
            assertEquals(ReportKind.CATEGORY_ONLY, summaries.first().kind)
            assertEquals(DiagnosticCategoryId.BATTERY, summaries.first().categoryId)
            assertNull(summaries[1].unavailableReason)
        }

    @Test
    fun duplicateIdIsRejectedAndOriginalReportRemainsUnchanged() =
        runBlocking {
            val original = report(id = "duplicate", completedAt = Instant.ofEpochMilli(2_000L))
            repository.insert(original)

            val failure =
                try {
                    repository.insert(report(id = "duplicate", completedAt = Instant.ofEpochMilli(3_000L)))
                    null
                } catch (exception: Exception) {
                    exception
                }

            assertNotNull(failure)
            assertEquals(ReportLoadResult.Available(original), repository.getById("duplicate"))
        }

    @Test
    fun corruptAndUnsupportedReportsReturnUnavailableWithoutBreakingHistory() =
        runBlocking {
            database.reportDao().insert(storedEntity(id = "corrupt", completedAt = 2_000L, payloadJson = "{broken"))
            database.reportDao().insert(
                storedEntity(
                    id = "future",
                    completedAt = 3_000L,
                    reportSchemaVersion = ReportSchemaVersion.CURRENT.value + 1,
                    payloadJson = "{}",
                ),
            )

            assertEquals(
                ReportLoadResult.Unavailable("corrupt", ReportReadFailure.CORRUPT_DATA),
                repository.getById("corrupt"),
            )
            assertEquals(
                ReportLoadResult.Unavailable("future", ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION),
                repository.getById("future"),
            )
            val summaries = repository.observeSummaries().first().associateBy(SavedReportSummary::stableId)
            assertEquals(ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION, summaries.getValue("future").unavailableReason)
            assertNull(summaries.getValue("corrupt").unavailableReason)
        }

    @Test
    fun comparisonLoadPreservesRequestedReportOrder() =
        runBlocking {
            val first = report(id = "first", completedAt = Instant.ofEpochMilli(3_000L))
            val second = report(id = "second", completedAt = Instant.ofEpochMilli(2_000L))
            repository.insert(first)
            repository.insert(second)

            assertEquals(
                ReportComparisonLoad(
                    first = ReportLoadResult.Available(first),
                    second = ReportLoadResult.Available(second),
                ),
                repository.getForComparison(first.stableId, second.stableId),
            )
        }

    @Test
    fun deleteAndDeleteAllRemoveOnlyTheRequestedReports() =
        runBlocking {
            val first = report(id = "first", completedAt = Instant.ofEpochMilli(2_000L))
            val second = report(id = "second", completedAt = Instant.ofEpochMilli(3_000L))
            repository.insert(first)
            repository.insert(second)

            repository.delete(first.stableId)

            assertEquals(ReportLoadResult.NotFound, repository.getById(first.stableId))
            assertEquals(ReportLoadResult.Available(second), repository.getById(second.stableId))
            repository.deleteAll()
            assertTrue(repository.observeSummaries().first().isEmpty())
        }

    private fun openDatabase(): FonecheckDatabase =
        Room
            .databaseBuilder(context, FonecheckDatabase::class.java, databaseName)
            .build()

    private fun report(
        id: String,
        completedAt: Instant,
        kind: ReportKind = ReportKind.FULL_CHECK,
    ): DiagnosticReport {
        val categoryId = DiagnosticCategoryId.BATTERY
        val values =
            listOf(
                EvidenceValue.BooleanValue(true),
                EvidenceValue.IntValue(7),
                EvidenceValue.LongValue(8L),
                EvidenceValue.DecimalValue(BigDecimal("9.50")),
                EvidenceValue.DoubleValue(10.25),
                EvidenceValue.RawTextValue("safe summary"),
                EvidenceValue.StableTextCodeValue("enabled"),
            )
        val evidence =
            values.mapIndexed { index, value ->
                DiagnosticEvidence(
                    categoryId = categoryId,
                    checkId = DiagnosticCheckId(categoryId, "battery.value_$index"),
                    status = DiagnosticStatus.PASS,
                    confidence = Confidence.HIGH,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    applicability = Applicability.APPLICABLE,
                    value = value,
                    unit = if (index == 1) EvidenceUnitCode("ms") else null,
                    capturedAt = completedAt.minusSeconds(index.toLong()),
                )
            }
        return DiagnosticReport(
            stableId = id,
            kind = kind,
            startedAt = completedAt.minusSeconds(30),
            completedAt = completedAt,
            device =
                ReportDeviceContext(
                    manufacturer = "Finnvek",
                    model = "Test Device",
                    brand = "fonecheck",
                    product = "repository-test",
                    androidRelease = "16",
                    apiLevel = 36,
                    securityPatch = "2026-08-01",
                ),
            app = ReportAppContext(versionName = "1.0.0", versionCode = 1L),
            categories = listOf(DiagnosticCategoryResult(categoryId, DiagnosticStatus.PASS, evidence)),
            score = ScoreSummary(ScoreVersion.CURRENT, value = 100, state = ScoreState.COMPLETE),
            coverage =
                CoverageSummary(
                    applicableCount = evidence.size,
                    completedCount = evidence.size,
                    notTestedCount = 0,
                    unavailableCount = 0,
                    percentage = 100,
                ),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }

    private fun storedEntity(
        id: String,
        completedAt: Long,
        reportSchemaVersion: Int = ReportSchemaVersion.CURRENT.value,
        payloadJson: String,
    ) = ReportEntity(
        id = id,
        reportKindCode = "full_check",
        categoryId = null,
        startedAtEpochMillis = 1_000L,
        completedAtEpochMillis = completedAt,
        reportSchemaVersion = reportSchemaVersion,
        scoreVersion = 1,
        scoreValue = 100,
        scoreStateCode = "complete",
        coveragePercentage = 100,
        applicableCount = 1,
        completedCount = 1,
        notTestedCount = 0,
        unavailableCount = 0,
        warningCount = 0,
        failureCount = 0,
        payloadJson = payloadJson,
    )
}
