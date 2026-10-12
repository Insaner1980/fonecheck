package com.insaner.fonecheck.ui.screens.home

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
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
import androidx.compose.ui.test.hasScrollToIndexAction
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
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
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
import com.insaner.fonecheck.navigation.DeviceInfo
import com.insaner.fonecheck.navigation.DiagnosticAccess
import com.insaner.fonecheck.navigation.History
import com.insaner.fonecheck.navigation.Report
import com.insaner.fonecheck.navigation.Settings
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.StatusLampSize
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
    fun firstVisitIntroductionDoesNotBlockHomeAndCanBeDismissed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var route: Any? = null
        var dismissed = false
        composeRule.setContent {
            FonecheckTheme {
                HomeContent(
                    latestFullCheck = LatestFullCheckState.Empty,
                    introduction = HomeIntroductionState(isVisible = true),
                    onNavigate = { route = it },
                    onRunAllTests = {},
                    onDismissIntroduction = { dismissed = true },
                )
            }
        }

        composeRule.onNodeWithTag("home_introduction").assertIsDisplayed()
        composeRule.onNodeWithTag("home_category_device").performScrollTo().performClick()
        assertEquals(DeviceInfo, route)
        composeRule.onNodeWithTag("home_introduction_dismiss").performScrollTo().performClick()
        assertTrue(dismissed)
        composeRule.onNodeWithText(context.getString(R.string.home_intro_privacy)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun statusPanelFitsNamesWithMatchingLegendColumns() {
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
            listOf("en", "fi", "sv", "nb", "da", "it", "pl", "tr", "tr-TR").map { tag ->
                val locale = Locale.forLanguageTag(tag)
                val configuration = Configuration(baseContext.resources.configuration)
                configuration.setLocale(locale)
                baseContext.createConfigurationContext(configuration)
            }
        var context by mutableStateOf(contexts.first())
        var screenWidth by mutableStateOf(412.dp)
        var fontScale by mutableFloatStateOf(1.15f)
        lateinit var columnMeasurement: HomeColumnMeasurement
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
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val density = LocalDensity.current
                        val spacing = FonecheckTheme.spacing
                        val textMeasurer = rememberTextMeasurer()
                        val names = diagnosticDestinations.map { stringResource(it.labelResId) }
                        val longestName =
                            names
                                .map { name ->
                                    name to
                                        textMeasurer
                                            .measure(
                                                text = AnnotatedString(name),
                                                style = FonecheckTheme.type.rowLabel,
                                                maxLines = 1,
                                                softWrap = false,
                                            ).size.width
                                }.maxBy { it.second }
                        // Derive the candidate name space from the viewport and rounded insets,
                        // independently of the rendered grid and the production column decision.
                        val panelWidth =
                            constraints.maxWidth -
                                with(density) { 2 * 6.dp.roundToPx() + 2 * spacing.md.roundToPx() }
                        val nameWidth =
                            with(density) {
                                (panelWidth - spacing.xs.roundToPx()) / 2 -
                                    2 * spacing.sm.roundToPx() - (StatusLampSize + spacing.sm).roundToPx()
                            }
                        SideEffect {
                            columnMeasurement =
                                HomeColumnMeasurement(
                                    longestName.first,
                                    longestName.second,
                                    nameWidth,
                                    panelWidth,
                                    density,
                                )
                        }
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
        }

        contexts.forEach { localizedContext ->
            listOf(
                412 to 1.15f,
                412 to 2f,
                320 to 1.15f,
                320 to 2f,
            ).forEach { (width, scale) ->
                composeRule.runOnIdle {
                    context = localizedContext
                    screenWidth = width.dp
                    fontScale = scale
                }
                val locale = context.resources.configuration.locales[0]
                composeRule.onNodeWithTag("home_category_device").performScrollTo()
                val measured = columnMeasurement
                val columns = if (measured.longestNameWidthPx <= measured.twoColumnNameWidthPx) 2 else 1
                val case =
                    "${locale.toLanguageTag()}, $width x 800 dp, font scale $scale, columns $columns, measurement=$measured"
                val deviceNode = composeRule.onNodeWithTag("home_category_device").fetchSemanticsNode()
                val performanceNode = composeRule.onNodeWithTag("home_category_performance").fetchSemanticsNode()
                val deviceBounds = deviceNode.unclippedBoundsInRoot()
                val performanceBounds = performanceNode.unclippedBoundsInRoot()
                val gridDetails = geometryDetails(case, deviceNode, performanceNode)
                Log.i("HomeColumnContract", gridDetails)
                if (columns == 2) {
                    assertEquals(gridDetails, deviceBounds.top, performanceBounds.top, 0.5f)
                    assertTrue(gridDetails, deviceBounds.right < performanceBounds.left)
                    assertEquals(
                        gridDetails,
                        measured.panelWidthPx.toFloat(),
                        performanceBounds.right - deviceBounds.left,
                        0.5f,
                    )
                } else {
                    assertEquals(gridDetails, deviceBounds.left, performanceBounds.left, 0.5f)
                    assertTrue(gridDetails, deviceBounds.bottom < performanceBounds.top)
                    assertEquals(gridDetails, measured.panelWidthPx.toFloat(), deviceBounds.width, 0.5f)
                }

                val cellHeights = mutableListOf<Float>()
                val cellGeometry = mutableListOf<String>()
                diagnosticDestinations.forEach { destination ->
                    val id = destination.category.stableId
                    composeRule.onNodeWithTag("home_reading_$id").assertDoesNotExist()
                    val cell = composeRule.onNodeWithTag("home_category_$id").performScrollTo()
                    cell.assertHeightIsAtLeast(48.dp)
                    composeRule.onNodeWithTag("home_category_reading_$id", useUnmergedTree = true).assertDoesNotExist()
                    val label = context.getString(destination.labelResId)
                    val labelNode =
                        composeRule
                            .onNode(
                                hasAnyAncestor(hasTestTag("home_category_$id")) and hasText(label),
                                useUnmergedTree = true,
                            ).assertIsDisplayed()
                    val nameLayouts = mutableListOf<TextLayoutResult>()
                    labelNode.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(nameLayouts) }
                    val cellNode = cell.fetchSemanticsNode()
                    val details = geometryDetails(case, cellNode, labelNode.fetchSemanticsNode())
                    assertFullTextFits(nameLayouts.single(), label, details, singleLine = columns == 2)
                    cellHeights += cellNode.unclippedBoundsInRoot().height
                    cellGeometry += details
                }
                if (columns == 2) {
                    cellHeights.forEach {
                        assertEquals("$case: heights=$cellHeights, cells=$cellGeometry", cellHeights.first(), it, 0.5f)
                    }
                }

                val passLabel = context.getString(R.string.run_all_status_pass).uppercase(locale)
                val failLabel = context.getString(R.string.run_all_status_fail).uppercase(locale)
                composeRule.onNodeWithText(passLabel).performScrollTo().assertIsDisplayed()
                val passNode = composeRule.onNodeWithText(passLabel).fetchSemanticsNode()
                val failNode = composeRule.onNodeWithText(failLabel).fetchSemanticsNode()
                val passBounds = passNode.unclippedBoundsInRoot()
                val failBounds = failNode.unclippedBoundsInRoot()
                val legendDetails = geometryDetails("$case: the legend must match the grid.", passNode, failNode)
                if (columns == 2) {
                    assertEquals(legendDetails, passBounds.top, failBounds.top, 0.5f)
                    assertTrue(legendDetails, passBounds.right < failBounds.left)
                } else {
                    assertEquals(legendDetails, passBounds.left, failBounds.left, 0.5f)
                    assertTrue(legendDetails, passBounds.bottom < failBounds.top)
                }
                composeRule.onNodeWithText(failLabel).performScrollTo().assertIsDisplayed()
                Log.i("HomeColumnContract", "COMPLETE: $gridDetails; $legendDetails")
            }
        }
    }

    @Test
    fun narrowStatusPanelWrapsLongLocalizedNamesAndLegendWithoutOverflow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration =
            Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag("es")) }
        val localizedContext = context.createConfigurationContext(configuration)
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList("es"))
                    .then(DeviceConfigurationOverride.FontScale(2f))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(240.dp, 800.dp))),
            ) {
                FonecheckTheme {
                    HomeContent(
                        latestFullCheck = LatestFullCheckState.Empty,
                        onNavigate = {},
                        onRunAllTests = {},
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }
        }

        diagnosticDestinations.forEach { destination ->
            val tag = "home_category_${destination.category.stableId}"
            val label = localizedContext.getString(destination.labelResId)
            composeRule.onNodeWithTag(tag).performScrollTo()
            val layouts = mutableListOf<TextLayoutResult>()
            composeRule
                .onNode(hasAnyAncestor(hasTestTag(tag)) and hasText(label), useUnmergedTree = true)
                .assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            val case = "es, 240 x 800 dp, font scale 2.0, columns 1"
            assertFullTextFits(layouts.single(), label, case)
            if (destination.category == DiagnosticCategoryId.BUTTONS) {
                assertTrue("The long volume-button label must wrap.", layouts.single().lineCount > 1)
            }
        }
        listOf(
            R.string.run_all_status_pass,
            R.string.run_all_status_fail,
            R.string.run_all_status_warning,
            R.string.run_all_status_info,
            R.string.status_not_available,
            R.string.status_not_measured,
        ).forEach { labelRes ->
            val label = localizedContext.getString(labelRes).uppercase(Locale.forLanguageTag("es"))
            val layouts = mutableListOf<TextLayoutResult>()
            composeRule
                .onNodeWithText(label, useUnmergedTree = true)
                .performScrollTo()
                .assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertFullTextFits(layouts.single(), label, "es, 240 x 800 dp, font scale 2.0, columns 1")
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
    fun diagnosticAvailabilityIsVisibleWithoutReplacingResultStatus() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        setHomeContent(LatestFullCheckState.Empty)

        diagnosticDestinations.forEach { destination ->
            val label =
                context.getString(
                    if (destination.access == DiagnosticAccess.FREE) {
                        R.string.access_free
                    } else {
                        R.string.access_full
                    },
                )
            composeRule
                .onNodeWithTag("home_category_access_${destination.category.stableId}", useUnmergedTree = true)
                .performScrollTo()
                .assertTextEquals(label)
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
            .onNodeWithText(context.resources.getQuantityString(R.plurals.home_latest_days_ago, 0, 0))
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

        val wordmarkNode =
            composeRule.onNodeWithText(context.getString(R.string.app_name), useUnmergedTree = true).assertIsDisplayed()
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
                .onNodeWithContentDescription(
                    context.getString(R.string.home_latest_past_title),
                    useUnmergedTree = true,
                ).assertIsDisplayed()
        val elapsedNode = composeRule.onNodeWithText(elapsedValue, useUnmergedTree = true).assertIsDisplayed()
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
        val coverageNode =
            composeRule
                .onNodeWithContentDescription(
                    coverage,
                    useUnmergedTree = true,
                ).assertIsDisplayed()
        val attentionNode =
            composeRule
                .onNodeWithContentDescription(
                    attention,
                    useUnmergedTree = true,
                ).assertIsDisplayed()
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
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(legendLabels.last()))
        composeRule.onNodeWithText(legendLabels.last(), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        val legendNodes =
            legendLabels.map { label ->
                composeRule.onNodeWithText(label, useUnmergedTree = true).fetchSemanticsNode()
            }
        val legendBounds = legendNodes.map { it.unclippedBoundsInRoot() }
        val legendDensity =
            legendNodes
                .first()
                .layoutInfo
                .density
                .density
        val legendDetails =
            geometryDetails(
                "dark, ${locale.toLanguageTag()}, " +
                    "${rootBounds.width / legendDensity} dp wide, font scale 2.0",
                *legendNodes.toTypedArray(),
            )
        legendBounds.zipWithNext().forEach { (first, second) ->
            assertTrue(
                "$legendDetails: the 200% legend entries must form six separate rows.",
                first.bottom <= second.top,
            )
        }
        assertTrue(
            "$legendDetails: the longest legend label must remain on one line at 200%.",
            legendBounds.last().height <= legendBounds.first().height * 1.25f,
        )
        legendLabels.forEach { label ->
            composeRule.onNodeWithText(label, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        }
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

    private data class HomeColumnMeasurement(
        val longestName: String,
        val longestNameWidthPx: Int,
        val twoColumnNameWidthPx: Int,
        val panelWidthPx: Int,
        val density: Density,
    )

    // SemanticsNode geometry is in pixels; getUnclippedBoundsInRoot() returns DpRect instead.
    private fun SemanticsNode.unclippedBoundsInRoot(): Rect = Rect(positionInRoot, size.toSize())

    private fun geometryDetails(
        configuration: String,
        vararg nodes: SemanticsNode,
    ): String {
        val scrollNode = composeRule.onNode(hasScrollToIndexAction()).fetchSemanticsNode()
        val scrollRange = scrollNode.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
        val bounds =
            nodes.joinToString { node ->
                val identity =
                    node.config.getOrNull(SemanticsProperties.TestTag)
                        ?: node.config.getOrNull(SemanticsProperties.Text)
                "$identity: clippedPx=${node.boundsInRoot}, unclippedPx=${node.unclippedBoundsInRoot()}, " +
                    "density=${node.layoutInfo.density}"
            }
        return "$configuration: nodes=[$bounds], viewportPx=${scrollNode.boundsInRoot}, scroll=$scrollRange"
    }

    private fun assertFullTextFits(
        layout: TextLayoutResult,
        label: String,
        configuration: String,
        singleLine: Boolean = false,
    ) {
        val lines =
            (0 until layout.lineCount).joinToString { line ->
                "$line: left=${layout.getLineLeft(line)}, right=${layout.getLineRight(line)}, " +
                    "top=${layout.getLineTop(line)}, bottom=${layout.getLineBottom(line)}, " +
                    "end=${layout.getLineEnd(line)}, visibleEnd=${layout.getLineEnd(line, visibleEnd = true)}"
            }
        val details =
            "$configuration: '$label', layoutSize=${layout.size}, paragraphWidth=${layout.multiParagraph.width}, " +
                "paragraphHeight=${layout.multiParagraph.height}, textLength=${label.length}, lines=[$lines]"
        assertEquals("$details: the full text must be laid out.", label, layout.layoutInput.text.text)
        assertFalse("$details: text must fit vertically.", layout.didOverflowHeight)
        if (singleLine) assertEquals("$details: text must stay on one line.", 1, layout.lineCount)
        // As in ReportRetestActionTest, allow only the half-pixel rounding of the measured IntSize.
        // Paragraph width can exceed measured width even when every visible line fits.
        for (line in 0 until layout.lineCount) {
            assertFalse("$details: line $line must not be ellipsised.", layout.isLineEllipsized(line))
            assertTrue("$details: line $line must fit on the left.", layout.getLineLeft(line) >= -0.5f)
            assertTrue(
                "$details: line $line must fit on the right.",
                layout.getLineRight(line) <= layout.size.width + 0.5f,
            )
        }
        assertEquals(
            "$details: the final character must be visible.",
            label.length,
            layout.getLineEnd(layout.lineCount - 1, visibleEnd = true),
        )
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
