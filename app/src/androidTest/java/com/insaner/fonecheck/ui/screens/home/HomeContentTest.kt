package com.insaner.fonecheck.ui.screens.home

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportReadFailure
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
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.navigation.History
import com.insaner.fonecheck.navigation.Report
import com.insaner.fonecheck.navigation.Settings
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun statusPanelShowsOnlySingleLineNamesWithMatchingLegendColumns() {
        val baseContext = InstrumentationRegistry.getInstrumentation().targetContext
        val savedValues =
            mapOf(
                DiagnosticCategoryId.PERFORMANCE to ("performance.cpu" to EvidenceValue.IntValue(8)),
                DiagnosticCategoryId.SIM to ("sim.network" to EvidenceValue.StableTextCodeValue("fourth_generation")),
                DiagnosticCategoryId.CAMERA to ("camera.inventory" to EvidenceValue.IntValue(2)),
                DiagnosticCategoryId.SENSORS to ("sensors.inventory" to EvidenceValue.IntValue(38)),
                DiagnosticCategoryId.STORAGE to ("storage.available" to EvidenceValue.LongValue(187_000_000_000L)),
            )
        val baseReport = report("grid-readings", List(diagnosticDestinations.size) { DiagnosticStatus.PASS }, 100)
        val savedReport =
            baseReport.copy(
                device = baseReport.device.copy(model = "Pixel 9"),
                categories =
                    baseReport.categories.map { category ->
                        val saved = savedValues[category.categoryId] ?: return@map category
                        category.copy(
                            evidence =
                                listOf(
                                    category.evidence.single().copy(
                                        checkId = DiagnosticCheckId(category.categoryId, saved.first),
                                        value = saved.second,
                                    ),
                                ),
                        )
                    },
            )
        val contexts =
            listOf(Locale.ENGLISH, Locale.forLanguageTag("fi")).map { locale ->
                val configuration = Configuration(baseContext.resources.configuration)
                configuration.setLocale(locale)
                baseContext.createConfigurationContext(configuration)
            }
        var context by mutableStateOf(contexts.first())
        var screenWidth by mutableStateOf(412.dp)
        var fontScale by mutableFloatStateOf(1.15f)
        composeRule.setContent {
            val locales =
                LocaleList(
                    context.resources.configuration.locales[0]
                        .toLanguageTag(),
                )
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(locales)
                    .then(DeviceConfigurationOverride.FontScale(fontScale))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(screenWidth, 800.dp))),
            ) {
                FonecheckTheme {
                    HomeContent(
                        latestFullCheck = LatestFullCheckState.Available(savedReport),
                        onNavigate = {},
                        onRunAllTests = {},
                        // Include the existing MainActivity housing inset as well as Home's own padding.
                        modifier = Modifier.padding(horizontal = 6.dp),
                        currentTime = Instant.parse("2026-08-11T10:30:00Z"),
                    )
                }
            }
        }

        contexts.forEach { localizedContext ->
            listOf(
                Triple(412, 1.15f, 2),
                Triple(412, 2f, 1),
                Triple(320, 1.15f, 1),
            ).forEach { (width, scale, columns) ->
                composeRule.runOnIdle {
                    context = localizedContext
                    screenWidth = width.dp
                    fontScale = scale
                }
                val locale = context.resources.configuration.locales[0]
                val case = "${locale.language}, $width dp, font scale $scale"
                composeRule.onNodeWithTag("home_category_device").performScrollTo()
                val deviceBounds = composeRule.onNodeWithTag("home_category_device").fetchSemanticsNode().boundsInRoot
                val performanceBounds =
                    composeRule.onNodeWithTag("home_category_performance").fetchSemanticsNode().boundsInRoot
                if (columns == 2) {
                    assertEquals(case, deviceBounds.top, performanceBounds.top, 0.5f)
                    assertTrue(case, deviceBounds.right < performanceBounds.left)
                } else {
                    assertEquals(case, deviceBounds.left, performanceBounds.left, 0.5f)
                    assertTrue(case, deviceBounds.bottom < performanceBounds.top)
                }

                val cellHeights = mutableListOf<Float>()
                diagnosticDestinations.forEach { destination ->
                    val id = destination.category.stableId
                    composeRule.onNodeWithTag("home_reading_$id").assertDoesNotExist()
                    val cell = composeRule.onNodeWithTag("home_category_$id").performScrollTo()
                    cell.assertHeightIsAtLeast(48.dp)
                    composeRule.onNodeWithTag("home_category_reading_$id", useUnmergedTree = true).assertDoesNotExist()
                    val label = context.getString(destination.labelResId)
                    cell.assertTextEquals(label)
                    val labelNode =
                        composeRule
                            .onNode(
                                hasAnyAncestor(hasTestTag("home_category_$id")) and hasText(label),
                                useUnmergedTree = true,
                            )
                    val nameLayouts = mutableListOf<TextLayoutResult>()
                    labelNode.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(nameLayouts) }
                    assertEquals("$case: $label must stay on one line.", 1, nameLayouts.single().lineCount)
                    assertFalse("$case: $label must fit its cell.", nameLayouts.single().didOverflowWidth)
                    assertFalse("$case: $label must not be ellipsised.", nameLayouts.single().isLineEllipsized(0))
                    cellHeights += cell.fetchSemanticsNode().boundsInRoot.height
                }
                cellHeights.forEach { assertEquals(cellHeights.first(), it, 0.5f) }

                val passLabel = context.getString(R.string.run_all_status_pass).uppercase(locale)
                val failLabel = context.getString(R.string.run_all_status_fail).uppercase(locale)
                composeRule.onNodeWithText(passLabel).performScrollTo()
                val passBounds = composeRule.onNodeWithText(passLabel).fetchSemanticsNode().boundsInRoot
                val failBounds = composeRule.onNodeWithText(failLabel).fetchSemanticsNode().boundsInRoot
                if (columns == 2) {
                    assertEquals("$case: the legend must match the grid.", passBounds.top, failBounds.top, 0.5f)
                    assertTrue(case, passBounds.right < failBounds.left)
                } else {
                    assertEquals("$case: the legend must match the grid.", passBounds.left, failBounds.left, 0.5f)
                    assertTrue(case, passBounds.bottom < failBounds.top)
                }
            }
        }
    }

    @Test
    fun noSavedFullCheckShowsTruthfulEmptyState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        setHomeContent(LatestFullCheckState.Empty)

        composeRule.onNodeWithText(context.getString(R.string.home_latest_empty_title)).assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_latest_empty")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_status_panel_title))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home_category_device").assertExists()
        composeRule.onNodeWithTag("home_category_biometrics").assertExists()
    }

    @Test
    fun loadingUnavailableAndErrorStatesStayExplicit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var state by mutableStateOf<LatestFullCheckState>(LatestFullCheckState.Loading)
        // The loading rule animates indefinitely, so the test clock is driven by hand while it is on
        // screen; an auto-advancing clock never reaches idle against an infinite animation.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            FonecheckTheme {
                HomeContent(
                    latestFullCheck = state,
                    onNavigate = {},
                    onRunAllTests = {},
                )
            }
        }
        composeRule.onNodeWithTag("home_latest_loading").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_loading)).assertIsDisplayed()
        composeRule.onNodeWithTag("home_latest_loading_indicator").assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(HOME_LOADING_INDICATOR_DELAY_MILLIS - 1L)
        composeRule.onNodeWithTag("home_latest_loading_indicator").assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(2L)
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag("home_latest_loading_indicator").assertIsDisplayed()

        state = LatestFullCheckState.Unavailable(ReportReadFailure.CORRUPT_DATA)
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_latest_unavailable").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.report_corrupt)).assertIsDisplayed()

        state = LatestFullCheckState.Error
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_latest_error").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_retry)).assertIsDisplayed()
    }

    @Test
    fun completedReportShowsPassedCountInsteadOfScore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("clean", listOf(DiagnosticStatus.PASS), score = 92)
        setHomeContent(LatestFullCheckState.Available(report))

        val label = context.getString(R.string.home_latest_passed_label)
        composeRule.onNodeWithText(label).assertIsDisplayed()
        composeRule.onNodeWithText("01").assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context
                    .getString(R.string.home_latest_passed_total, "1"),
            ).assertIsDisplayed()
        assertPassedCount(context, passed = "1", total = "1")
        composeRule.onNodeWithContentDescription(coverageValue(context, 1.0)).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_latest_no_attention))
            .assertIsDisplayed()
        // The score is still in the report; the home screen no longer renders one.
        composeRule.onNodeWithText("92").assertDoesNotExist()
    }

    @Test
    fun staleReportShowsElapsedAgeAndKeepsItsExactCompletionTime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("stale", listOf(DiagnosticStatus.PASS), score = 92)
        setHomeContent(
            state = LatestFullCheckState.Available(report),
            currentTime = Instant.parse("2026-08-20T11:00:00Z"),
        )

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_latest_past_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.resources.getQuantityString(R.plurals.home_latest_days_ago, 9, 9))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.home_latest_completed_at,
                    formatHomeCompletedAt(
                        report.completedAt,
                        context.resources.configuration.locales[0],
                        ZoneId.systemDefault(),
                    ),
                ),
            ).assertIsDisplayed()
    }

    @Test
    fun freshReportKeepsTheExistingHeaderWithoutAStaleMarker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("fresh", listOf(DiagnosticStatus.PASS), score = 92)
        setHomeContent(
            state = LatestFullCheckState.Available(report),
            currentTime = report.completedAt.plusSeconds(23 * 60 * 60),
        )

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_latest_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_latest_past_title))
            .assertDoesNotExist()
        composeRule
            .onNodeWithText(
                formatHomeCompletedAt(
                    report.completedAt,
                    context.resources.configuration.locales[0],
                    ZoneId.systemDefault(),
                ),
            ).assertIsDisplayed()
    }

    @Test
    fun warningAndFailureReportShowsAttentionSummary() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report =
            report(
                id = "attention",
                statuses = listOf(DiagnosticStatus.WARNING, DiagnosticStatus.FAIL),
                score = 44,
            )
        setHomeContent(LatestFullCheckState.Available(report))

        composeRule
            .onNodeWithContentDescription(
                context.resources.getQuantityString(
                    R.plurals.home_latest_evidence_attention_summary,
                    2,
                    2,
                ),
            ).assertIsDisplayed()
        assertPassedCount(context, passed = "0", total = "2")
        composeRule.onNodeWithTag("home_category_device").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home_category_performance").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun incompleteReportReportsWhatWasPassedWithoutInventingOne() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report =
            report(
                id = "incomplete",
                statuses = listOf(DiagnosticStatus.NOT_TESTED, DiagnosticStatus.PASS),
                score = null,
                scoreState = ScoreState.INCOMPLETE,
                coveragePercentage = 50,
            )
        setHomeContent(LatestFullCheckState.Available(report))

        assertPassedCount(context, passed = "1", total = "2")
        composeRule.onNodeWithContentDescription(coverageValue(context, 0.5)).assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_category_device")
            .performScrollTo()
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.status_not_measured),
                ),
            )
    }

    @Test
    fun headerAndLatestReportNavigateToExistingTypedRoutes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var route: Any? = null
        val report = report("report-42", listOf(DiagnosticStatus.PASS), score = 90)
        setHomeContent(LatestFullCheckState.Available(report), onNavigate = { route = it })

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
            .performClick()
        assertEquals(History, route)
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
            .performClick()
        assertEquals(Settings, route)
        composeRule.onNodeWithTag("home_latest_report_card").performClick()
        assertEquals(Report("report-42"), route)
    }

    @Test
    fun headerButtonsAndReportExposeRequiredSemanticsAndTouchTargets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("semantic", listOf(DiagnosticStatus.PASS), score = 90)
        setHomeContent(LatestFullCheckState.Available(report))

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
            .assertHeightIsAtLeast(48.dp)
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
            .assertHeightIsAtLeast(48.dp)
        composeRule
            .onNodeWithTag("home_latest_report_card")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun statusPanelUsesTheLatestFullCheckAndKeepsAllNavigationTargets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var route: Any? = null
        val report = report("category-row", listOf(DiagnosticStatus.PASS), score = 90)
        setHomeContent(LatestFullCheckState.Available(report), onNavigate = { route = it })

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_status_panel_title))
            .assertIsDisplayed()
        diagnosticDestinations.forEach { destination ->
            composeRule
                .onNodeWithTag("home_category_${destination.category.stableId}")
                .performScrollTo()
                .assertHeightIsAtLeast(48.dp)
                .performClick()
            assertEquals(destination.route, route)
        }
        composeRule
            .onNodeWithTag("home_category_performance")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.value_unavailable_short),
                ),
            )
    }

    @Test
    fun statusPanelUsesCanonicalVerdictsForAllCategories() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report =
            report(
                id = "category-results",
                statuses =
                    listOf(
                        DiagnosticStatus.PASS,
                        DiagnosticStatus.WARNING,
                        DiagnosticStatus.INFO,
                        DiagnosticStatus.NOT_TESTED,
                        DiagnosticStatus.PASS,
                        DiagnosticStatus.PASS,
                        DiagnosticStatus.PASS,
                        DiagnosticStatus.PASS,
                        DiagnosticStatus.PASS,
                        DiagnosticStatus.INFO,
                        DiagnosticStatus.NOT_AVAILABLE,
                    ),
                score = 90,
            )

        setHomeContent(LatestFullCheckState.Available(report))

        composeRule
            .onNodeWithTag("home_category_performance")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.run_all_status_warning),
                ),
            )
        composeRule.onNodeWithTag("home_category_performance").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_category_sim")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.run_all_status_info),
                ),
            )
        composeRule.onNodeWithTag("home_category_sim").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_category_display")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.status_not_measured),
                ),
            )
        composeRule.onNodeWithTag("home_category_display").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_category_battery")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.run_all_status_pass),
                ),
            )
        composeRule.onNodeWithTag("home_category_battery").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_category_thermal")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.run_all_status_info),
                ),
            )
        composeRule.onNodeWithTag("home_category_thermal").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithTag("home_category_storage")
            .performScrollTo()
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    context.getString(R.string.status_not_available),
                ),
            )
        val locale = context.resources.configuration.locales[0]
        listOf(
            R.string.run_all_status_pass,
            R.string.run_all_status_fail,
            R.string.run_all_status_warning,
            R.string.run_all_status_info,
            R.string.status_not_available,
            R.string.status_not_measured,
        ).forEach { labelResId ->
            composeRule
                .onAllNodesWithText(context.getString(labelResId).uppercase(locale))
                .assertCountEquals(1)
        }
    }

    @Test
    fun lightThemeRendersPrimaryHomeEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FonecheckTheme(darkTheme = false) {
                HomeContent(
                    latestFullCheck = LatestFullCheckState.Empty,
                    onNavigate = {},
                    onRunAllTests = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.home_start_full_check))
            .assertIsDisplayed()
    }

    @Test
    fun darkThemeRemainsScrollableAtTwoHundredPercentFontScale() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f),
            ) {
                FonecheckTheme(darkTheme = true) {
                    HomeContent(
                        latestFullCheck =
                            LatestFullCheckState.Available(
                                report(
                                    id = "font-scale",
                                    statuses = listOf(DiagnosticStatus.WARNING, DiagnosticStatus.FAIL),
                                    score = 44,
                                ),
                            ),
                        onNavigate = {},
                        onRunAllTests = {},
                        currentTime = Instant.parse("2026-08-24T10:00:00Z"),
                    )
                }
            }
        }

        val wordmarkNode = composeRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        val historyNode =
            composeRule
                .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
                .assertIsDisplayed()
        val settingsNode =
            composeRule
                .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
                .assertIsDisplayed()
        val wordmarkBounds = wordmarkNode.fetchSemanticsNode().boundsInRoot
        val historyBounds = historyNode.fetchSemanticsNode().boundsInRoot
        val settingsBounds = settingsNode.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "The wordmark and header actions must remain on the same row at 200%.",
            wordmarkBounds.bottom > historyBounds.top && wordmarkBounds.top < historyBounds.bottom,
        )
        assertTrue(
            "Both header actions must remain on the wordmark row at 200%.",
            wordmarkBounds.bottom > settingsBounds.top && wordmarkBounds.top < settingsBounds.bottom,
        )

        val elapsedValue = context.resources.getQuantityString(R.plurals.home_latest_days_ago, 13, 13)
        val latestLabelNode =
            composeRule
                .onNodeWithContentDescription(context.getString(R.string.home_latest_past_title))
                .assertIsDisplayed()
        val elapsedNode = composeRule.onNodeWithText(elapsedValue).assertIsDisplayed()
        val latestLabelBounds = latestLabelNode.fetchSemanticsNode().boundsInRoot
        val elapsedBounds = elapsedNode.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "The latest-check label must stay on one line at 200%.",
            latestLabelBounds.height <= elapsedBounds.height * 1.25f,
        )
        assertTrue(
            "The latest-check value must sit below its label at 200%.",
            latestLabelBounds.bottom <= elapsedBounds.top,
        )

        val coverage = coverageValue(context, 1.0)
        val attention =
            context.resources.getQuantityString(
                R.plurals.home_latest_evidence_attention_summary,
                2,
                2,
            )
        val coverageNode = composeRule.onNodeWithContentDescription(coverage).assertIsDisplayed()
        val attentionNode = composeRule.onNodeWithContentDescription(attention).assertIsDisplayed()
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val coverageBounds = coverageNode.fetchSemanticsNode().boundsInRoot
        val attentionBounds = attentionNode.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "The 200% metrics must occupy separate vertical lines.",
            coverageBounds.bottom <= attentionBounds.top,
        )
        assertTrue(
            "The 200% coverage metric must fit within the visible Home bounds.",
            coverageBounds.left >= rootBounds.left && coverageBounds.right <= rootBounds.right,
        )
        assertTrue(
            "The 200% attention metric must fit within the visible Home bounds.",
            attentionBounds.left >= rootBounds.left && attentionBounds.right <= rootBounds.right,
        )
        val locale = homeUiLanguageLocale(context.resources.configuration.locales[0])
        val legendLabels =
            listOf(
                R.string.run_all_status_pass,
                R.string.run_all_status_fail,
                R.string.run_all_status_warning,
                R.string.run_all_status_info,
                R.string.status_not_available,
                R.string.status_not_measured,
            ).map { context.getString(it).uppercase(locale) }
        composeRule.onNodeWithText(legendLabels.last()).performScrollTo()
        val legendBounds =
            legendLabels.map { label ->
                composeRule.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
            }
        legendBounds.zipWithNext().forEach { (first, second) ->
            assertTrue(
                "The 200% legend entries must form six separate rows.",
                first.bottom <= second.top,
            )
        }
        assertTrue(
            "The longest legend label must remain on one line at 200%.",
            legendBounds.last().height <= legendBounds.first().height * 1.25f,
        )
        composeRule
            .onNodeWithTag("home_category_biometrics")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun rightToLeftLayoutKeepsHeaderActionsAndLatestReportAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("rtl", listOf(DiagnosticStatus.PASS), score = 90)
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FonecheckTheme {
                    HomeContent(
                        latestFullCheck = LatestFullCheckState.Available(report),
                        onNavigate = {},
                        onRunAllTests = {},
                    )
                }
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home_latest_report_card").assertIsDisplayed()
    }

    /** The figures are drawn as one run of styled text, so the counts are read back spoken. */
    private fun assertPassedCount(
        context: Context,
        passed: String,
        total: String,
    ) {
        val spoken = context.getString(R.string.home_latest_passed_description, passed, total)
        composeRule
            .onNodeWithTag("home_latest_report_card")
            .assert(
                SemanticsMatcher("state description contains '$spoken'") { node ->
                    node.config.getOrNull(SemanticsProperties.StateDescription)?.contains(spoken) == true
                },
            )
    }

    private fun coverageValue(
        context: Context,
        fraction: Double,
    ): String {
        val locale = homeUiLanguageLocale(context.resources.configuration.locales[0])
        return context.getString(
            R.string.home_latest_coverage_value,
            NumberFormat.getPercentInstance(locale).format(fraction),
        )
    }

    private fun setHomeContent(
        state: LatestFullCheckState,
        currentTime: Instant = Instant.parse("2026-08-11T10:30:00Z"),
        onNavigate: (Any) -> Unit = {},
    ) {
        composeRule.setContent {
            FonecheckTheme {
                HomeContent(
                    latestFullCheck = state,
                    onNavigate = onNavigate,
                    onRunAllTests = {},
                    currentTime = currentTime,
                )
            }
        }
    }

    private fun report(
        id: String,
        statuses: List<DiagnosticStatus>,
        score: Int?,
        scoreState: ScoreState = if (score == null) ScoreState.INCOMPLETE else ScoreState.COMPLETE,
        coveragePercentage: Int = 100,
    ): DiagnosticReport {
        val categories =
            statuses.mapIndexed { index, status ->
                val categoryId = DiagnosticCategoryId.entries[index]
                val evidence =
                    DiagnosticEvidence(
                        categoryId = categoryId,
                        checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.home_test"),
                        status = status,
                        confidence = Confidence.HIGH,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                        applicability = Applicability.APPLICABLE,
                        capturedAt = Instant.parse("2026-08-11T10:00:00Z"),
                    )
                DiagnosticCategoryResult(categoryId, status, listOf(evidence))
            }
        val completedCount = statuses.count { it != DiagnosticStatus.NOT_TESTED }
        return DiagnosticReport(
            stableId = id,
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-11T09:59:00Z"),
            completedAt = Instant.parse("2026-08-11T10:00:00Z"),
            device = ReportDeviceContext("Test", "Phone", "Test", "test", "16", 36, null),
            app = ReportAppContext("1.0", 1),
            categories = categories,
            score = ScoreSummary(ScoreVersion.CURRENT, score, scoreState),
            coverage =
                CoverageSummary(
                    applicableCount = statuses.size,
                    completedCount = completedCount,
                    notTestedCount = statuses.size - completedCount,
                    unavailableCount = 0,
                    percentage = coveragePercentage,
                ),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }
}
