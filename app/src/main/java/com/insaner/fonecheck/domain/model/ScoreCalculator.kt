package com.insaner.fonecheck.domain.model

data class ScoreCalculation(
    val score: ScoreSummary,
    val coverage: CoverageSummary,
)

object ScoreCalculator {
    fun calculate(categories: List<DiagnosticCategoryResult>): ScoreCalculation {
        val evidence = categories.flatMap(DiagnosticCategoryResult::evidence)
        val unavailableCount = evidence.count {
            it.status == DiagnosticStatus.NOT_AVAILABLE || it.applicability == Applicability.NOT_APPLICABLE
        }
        val applicableEvidence = evidence.filter {
            it.status != DiagnosticStatus.NOT_AVAILABLE && it.applicability == Applicability.APPLICABLE
        }
        val completedCount = applicableEvidence.count { it.status in COMPLETED_STATUSES }
        val notTestedCount = applicableEvidence.count { it.status == DiagnosticStatus.NOT_TESTED }
        val coverage = CoverageSummary(
            applicableCount = applicableEvidence.size,
            completedCount = completedCount,
            notTestedCount = notTestedCount,
            unavailableCount = unavailableCount,
            percentage =
                if (applicableEvidence.isEmpty()) 0 else completedCount * 100 / applicableEvidence.size,
        )
        val categoryScores = categories.mapNotNull(::scoreCategory)
        val scoreValue = categoryScores.takeIf { it.isNotEmpty() }?.averageFloor()
        val scoreState = when {
            scoreValue == null || coverage.percentage < PARTIAL_COVERAGE_PERCENTAGE -> ScoreState.INCOMPLETE
            coverage.percentage == 100 -> ScoreState.COMPLETE
            else -> ScoreState.PARTIAL
        }

        return ScoreCalculation(
            score = ScoreSummary(
                ScoreVersion.CURRENT,
                if (scoreState == ScoreState.INCOMPLETE) null else scoreValue,
                scoreState,
            ),
            coverage = coverage,
        )
    }

    private fun scoreCategory(category: DiagnosticCategoryResult): Int? =
        category.evidence
            .filter { it.applicability == Applicability.APPLICABLE }
            .mapNotNull { SCORE_POINTS[it.status] }
            .takeIf { it.isNotEmpty() }
            ?.averageFloor()

    private fun List<Int>.averageFloor(): Int = sum() / size

    private val COMPLETED_STATUSES = setOf(
        DiagnosticStatus.PASS,
        DiagnosticStatus.FAIL,
        DiagnosticStatus.WARNING,
        DiagnosticStatus.INFO,
    )

    private val SCORE_POINTS = mapOf(
        DiagnosticStatus.PASS to 100,
        DiagnosticStatus.WARNING to 65,
        DiagnosticStatus.FAIL to 0,
    )

    private const val PARTIAL_COVERAGE_PERCENTAGE = 70
}
