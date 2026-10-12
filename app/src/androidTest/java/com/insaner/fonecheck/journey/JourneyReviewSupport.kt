package com.insaner.fonecheck.journey

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.insaner.fonecheck.ui.screens.runall.ReportSaveStatus
import com.insaner.fonecheck.ui.screens.runall.RunAllStage
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

/**
 * Walks every automatic-stage review in plan order. Each review must be awaiting continue in the
 * automatic stage with no report built or saved before [continueReview] moves past it.
 */
internal fun continueAutomaticStageReviews(
    compose: ComposeTestRule,
    run: RunAllTestsViewModel,
    continueReview: () -> Unit,
) {
    val categories =
        run.state.value.plan.categories
            .filter { it.stage == RunAllStage.AUTOMATIC }
    assertTrue(categories.isNotEmpty())
    categories.forEach { category ->
        compose.waitUntil(10_000L) {
            val state = run.state.value
            state.stage == RunAllStage.AUTOMATIC && state.awaitingContinue && state.reviewCategory == category
        }
        compose.waitForIdle()
        val state = run.state.value
        assertEquals(RunAllStage.AUTOMATIC, state.stage)
        assertNull(state.report)
        assertEquals(ReportSaveStatus.IDLE, state.saveStatus)
        continueReview()
    }
}
