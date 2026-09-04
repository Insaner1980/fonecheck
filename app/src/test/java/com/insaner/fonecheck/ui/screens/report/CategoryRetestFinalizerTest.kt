package com.insaner.fonecheck.ui.screens.report

import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.testing.testReport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CategoryRetestFinalizerTest {
    @Test
    fun retestFreezesAndSavesANewCategoryOnlyReportWithoutChangingTheSource() =
        runTest {
            val repository = FakeReportRepository()
            val finalizer =
                CategoryRetestFinalizer(
                    clock = EpochMillisClock { 2_000L },
                    idProvider = IdProvider { "retest-report" },
                    reportRepository = repository,
                )
            val sourceReportId = "original-full-report"
            val sourceReport = testReport(id = sourceReportId)
            repository.insert(sourceReport)

            val retest =
                finalizer.freeze(
                    startedAt = Instant.ofEpochMilli(1_000L),
                    device = deviceContext(),
                    app = appContext(),
                    snapshot = snapshot(),
                )

            assertEquals(ReportKind.CATEGORY_ONLY, retest.kind)
            assertEquals(listOf(DiagnosticCategoryId.DISPLAY), retest.categories.map { it.categoryId })
            assertNotEquals(sourceReportId, retest.stableId)
            assertTrue(finalizer.save(retest))
            assertEquals(retest, (repository.getById("retest-report") as ReportLoadResult.Available).report)
            assertEquals(sourceReport, (repository.getById(sourceReportId) as ReportLoadResult.Available).report)
        }

    private fun snapshot() =
        DiagnosticCategorySnapshot(
            version = DiagnosticSnapshotVersion.CURRENT,
            categoryId = DiagnosticCategoryId.DISPLAY,
            evidence =
                listOf(
                    DiagnosticEvidence(
                        categoryId = DiagnosticCategoryId.DISPLAY,
                        checkId = DiagnosticCheckId(DiagnosticCategoryId.DISPLAY, "display.visual"),
                        status = DiagnosticStatus.PASS,
                        confidence = Confidence.HIGH,
                        source = EvidenceSource.USER_CONFIRMATION,
                        applicability = Applicability.APPLICABLE,
                        capturedAt = Instant.ofEpochMilli(1_500L),
                    ),
                ),
        )

    private fun deviceContext() =
        ReportDeviceContext(
            manufacturer = "manufacturer",
            model = "model",
            brand = "brand",
            product = "product",
            androidRelease = "16",
            apiLevel = 36,
            securityPatch = "2026-08-01",
        )

    private fun appContext() = ReportAppContext(versionName = "1.0.0", versionCode = 1L)
}
