package com.insaner.fonecheck.ui.screens.report

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.localization.stableCodeDisplayText
import java.time.Duration

data class ReportCategoryCounts(
    val pass: Int,
    val warning: Int,
    val fail: Int,
    val info: Int,
    val notAvailable: Int,
    val notTested: Int,
)

data class ReportDetailPresentation(
    val categories: List<DiagnosticCategoryResult>,
    val counts: ReportCategoryCounts,
    val durationMillis: Long,
)

object ReportDetailPresenter {
    fun present(report: DiagnosticReport): ReportDetailPresentation {
        val reportedCategories = report.categories.associateBy(DiagnosticCategoryResult::categoryId)
        val categories =
            DiagnosticCatalog.categories.map { categoryId ->
                reportedCategories[categoryId]
                    ?: DiagnosticCategoryResult(
                        categoryId = categoryId,
                        aggregateStatus = DiagnosticStatus.NOT_TESTED,
                        evidence = emptyList(),
                    )
            }
        return ReportDetailPresentation(
            categories = categories,
            counts =
                ReportCategoryCounts(
                    pass = categories.count { it.aggregateStatus == DiagnosticStatus.PASS },
                    warning = categories.count { it.aggregateStatus == DiagnosticStatus.WARNING },
                    fail = categories.count { it.aggregateStatus == DiagnosticStatus.FAIL },
                    info = categories.count { it.aggregateStatus == DiagnosticStatus.INFO },
                    notAvailable = categories.count { it.aggregateStatus == DiagnosticStatus.NOT_AVAILABLE },
                    notTested = categories.count { it.aggregateStatus == DiagnosticStatus.NOT_TESTED },
                ),
            durationMillis = Duration.between(report.startedAt, report.completedAt).toMillis().coerceAtLeast(0L),
        )
    }
}

fun stableCodeFallback(code: String): String = stableCodeDisplayText(code)
