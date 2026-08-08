package com.insaner.fonecheck.ui.screens.report

import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportDetailPresenterTest {
    @Test
    fun missingCanonicalCategoriesAreRenderedAsNotTestedWithoutChangingNotAvailable() {
        val presentation =
            ReportDetailPresenter.present(
                report(
                    categories =
                        listOf(
                            category(DiagnosticCategoryId.DEVICE, DiagnosticStatus.PASS),
                            category(DiagnosticCategoryId.CAMERA, DiagnosticStatus.NOT_AVAILABLE),
                        ),
                ),
            )

        assertEquals(DiagnosticCatalog.categories, presentation.categories.map { it.categoryId })
        assertEquals(DiagnosticStatus.PASS, presentation.categories[0].aggregateStatus)
        assertEquals(
            DiagnosticStatus.NOT_AVAILABLE,
            presentation.categories.single { it.categoryId == DiagnosticCategoryId.CAMERA }.aggregateStatus,
        )
        assertEquals(
            DiagnosticStatus.NOT_TESTED,
            presentation.categories.single { it.categoryId == DiagnosticCategoryId.AUDIO }.aggregateStatus,
        )
        assertEquals(1, presentation.counts.notAvailable)
        assertEquals(12, presentation.counts.notTested)
    }

    @Test
    fun categoryOnlyReportKeepsOneMeasuredCategoryAndFourteenCanonicalRows() {
        val presentation =
            ReportDetailPresenter.present(
                report(
                    kind = ReportKind.CATEGORY_ONLY,
                    categories = listOf(category(DiagnosticCategoryId.STORAGE, DiagnosticStatus.WARNING)),
                ),
            )

        assertEquals(14, presentation.categories.size)
        assertEquals(1, presentation.counts.warning)
        assertEquals(13, presentation.counts.notTested)
        assertEquals(60_000L, presentation.durationMillis)
    }

    @Test
    fun futureStableCodesHaveAReadableFallback() {
        assertEquals("Future vendor state", stableCodeFallback("future_vendor_state"))
    }

    private fun category(
        id: DiagnosticCategoryId,
        status: DiagnosticStatus,
    ) = DiagnosticCategoryResult(id, status, emptyList())

    private fun report(
        kind: ReportKind = ReportKind.FULL_CHECK,
        categories: List<DiagnosticCategoryResult>,
    ) =
        DiagnosticReport(
            stableId = "report-detail",
            kind = kind,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device =
                ReportDeviceContext(
                    manufacturer = "Finnvek",
                    model = "Test Device",
                    brand = "Fonecheck",
                    product = "test-product",
                    androidRelease = "16",
                    apiLevel = 36,
                    securityPatch = "2026-08-01",
                ),
            app = ReportAppContext("1.0.0", 1L),
            categories = categories,
            score = ScoreSummary(ScoreVersion.CURRENT, 82, ScoreState.PARTIAL),
            coverage = CoverageSummary(14, categories.size, 14 - categories.size, 0, 50),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
}
