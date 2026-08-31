package com.insaner.fonecheck.ui.screens.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.data.repository.SavedReportSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.ui.format.formatUiDateTime
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reportsExposeOpenCompareExportAndConfirmedDeleteActions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var opened: String? = null
        var compared: Pair<String, String>? = null
        var exported: String? = null
        var deleted: String? = null
        composeRule.setContent {
            FonecheckTheme {
                HistoryScreen(
                    state =
                        HistoryState(
                            reports =
                                listOf(
                                    summary("newer", "2026-08-08T11:00:00Z", ScoreState.COMPLETE),
                                    summary("older", "2026-08-08T10:00:00Z", ScoreState.PARTIAL),
                                    summary(
                                        id = "storage-retest",
                                        completedAt = "2026-08-08T09:00:00Z",
                                        scoreState = ScoreState.COMPLETE,
                                        kind = ReportKind.CATEGORY_ONLY,
                                        categoryId = DiagnosticCategoryId.STORAGE,
                                    ),
                                ),
                            isLoading = false,
                        ),
                    onRetry = {},
                    onOpen = { opened = it },
                    onCompare = { first, second -> compared = first to second },
                    onExport = { exported = it },
                    onDelete = { deleted = it },
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.history_full_check))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("history_open_newer").performClick()
        assertEquals("newer", opened)
        composeRule.onNodeWithTag("history_compare_newer").performClick()
        composeRule.onNodeWithTag("history_compare_older").performScrollTo().performClick()
        assertEquals("newer" to "older", compared)
        composeRule.onNodeWithTag("history_export_older").performClick()
        assertEquals("older", exported)
        composeRule.onNodeWithTag("history_delete_older").performClick()
        composeRule.onNodeWithTag("history_confirm_delete").performClick()
        assertEquals("older", deleted)
        composeRule
            .onNodeWithText(context.getString(R.string.history_status))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.history_status_partial))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                formatUiDateTime(
                    Instant.parse("2026-08-08T10:00:00Z"),
                    context.resources.configuration.locales[0],
                    ZoneId.systemDefault(),
                ),
            ).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                context.getString(R.string.history_category_retest),
            ).performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.home_cat_storage))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.history_status_complete))
            .assertIsDisplayed()
    }

    @Test
    fun categoryRetestShowsSeparateWarningAndIssueRows() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        setHistoryContent(
            HistoryState(
                reports =
                    listOf(
                        summary(
                            id = "storage-retest",
                            completedAt = "2026-08-08T09:00:00Z",
                            scoreState = ScoreState.COMPLETE,
                            kind = ReportKind.CATEGORY_ONLY,
                            categoryId = DiagnosticCategoryId.STORAGE,
                            warningCount = 3,
                            failureCount = 2,
                        ),
                    ),
                isLoading = false,
            ),
        )

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.history_category_retest))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_cat_storage)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.history_warnings))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.history_issues))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun fullAndUnavailableHeadersIgnoreStoredCategoryId() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        setHistoryContent(
            HistoryState(
                reports =
                    listOf(
                        summary(
                            id = "full-with-category",
                            completedAt = "2026-08-08T10:00:00Z",
                            scoreState = ScoreState.COMPLETE,
                            categoryId = DiagnosticCategoryId.STORAGE,
                        ),
                        summary(
                            id = "unavailable-with-category",
                            completedAt = "2026-08-08T09:00:00Z",
                            scoreState = ScoreState.COMPLETE,
                            unavailableReason = ReportReadFailure.CORRUPT_DATA,
                            kind = ReportKind.CATEGORY_ONLY,
                            categoryId = DiagnosticCategoryId.STORAGE,
                        ),
                    ),
                isLoading = false,
            ),
        )

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.history_full_check))
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.history_unavailable_report))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.home_cat_storage))
            .assertDoesNotExist()
    }

    @Test
    fun loadingEmptyAndErrorWithExistingContentStayExplicit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var state by mutableStateOf(HistoryState())
        composeRule.setContent {
            FonecheckTheme {
                HistoryScreen(
                    state = state,
                    onRetry = {},
                    onOpen = {},
                    onCompare = { _, _ -> },
                    onExport = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.history_loading)).assertIsDisplayed()
        state = HistoryState(isLoading = false)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.history_empty)).assertIsDisplayed()
        state =
            HistoryState(
                reports = listOf(summary("still-visible", "2026-08-08T10:00:00Z", ScoreState.COMPLETE)),
                isLoading = false,
                error = "history_load_failed",
            )
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.history_error)).assertIsDisplayed()
        composeRule.onNodeWithText("still-visible").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun unavailableSummaryHidesInvalidScoreAndLabelsTheEntry() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        setHistoryContent(
            HistoryState(
                reports =
                    listOf(
                        summary(
                            id = "corrupt",
                            completedAt = "2026-08-08T10:00:00Z",
                            scoreState = ScoreState.COMPLETE,
                            scoreValue = 99,
                            unavailableReason = ReportReadFailure.CORRUPT_DATA,
                        ),
                    ),
                isLoading = false,
            ),
        )

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.history_unavailable_report))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.history_status_unavailable)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.value_unavailable_short)).assertIsDisplayed()
        composeRule.onNodeWithText("99").assertDoesNotExist()
    }

    private fun setHistoryContent(state: HistoryState) {
        composeRule.setContent {
            FonecheckTheme {
                HistoryScreen(
                    state = state,
                    onRetry = {},
                    onOpen = {},
                    onCompare = { _, _ -> },
                    onExport = {},
                    onDelete = {},
                )
            }
        }
    }

    private fun summary(
        id: String,
        completedAt: String,
        scoreState: ScoreState?,
        scoreValue: Int? = 92,
        unavailableReason: ReportReadFailure? = null,
        kind: ReportKind = ReportKind.FULL_CHECK,
        categoryId: DiagnosticCategoryId? = null,
        warningCount: Int = 1,
        failureCount: Int = 0,
    ) = SavedReportSummary(
        stableId = id,
        kind = kind,
        categoryId = categoryId,
        completedAt = Instant.parse(completedAt),
        reportSchemaVersion = 1,
        scoreVersion = 1,
        scoreValue = scoreValue,
        scoreState = scoreState,
        coveragePercentage = 100,
        warningCount = warningCount,
        failureCount = failureCount,
        unavailableReason = unavailableReason,
    )
}
