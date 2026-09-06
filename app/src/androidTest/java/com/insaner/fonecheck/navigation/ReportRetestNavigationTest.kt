package com.insaner.fonecheck.navigation

import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.data.repository.RoomReportRepository
import com.insaner.fonecheck.di.DatabaseModule
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.export.AndroidReportExporter
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.ui.MainActivity
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonDirection
import com.insaner.fonecheck.ui.screens.runall.ReportSaveStatus
import com.insaner.fonecheck.ui.screens.runall.RunAllHardwareProfile
import com.insaner.fonecheck.ui.screens.runall.RunAllPermissions
import com.insaner.fonecheck.ui.screens.runall.RunAllSelections
import com.insaner.fonecheck.ui.screens.runall.RunAllStage
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsState
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsViewModel
import com.insaner.fonecheck.ui.screens.runall.retestFixture
import com.insaner.fonecheck.ui.screens.runall.scrollToReportText
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class ReportRetestNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: NavHostController
    private val sessions = ConcurrentHashMap<String, RunAllTestsViewModel>()
    private var renderVersion by mutableIntStateOf(0)
    private var renderedVersion = 0

    @Test
    fun realNavHostOwnsFreshConsecutiveRunsAndReturnsToUnchangedReports() {
        val initialReportIds = savedReportIds()
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                val nav = rememberNavController()
                val entry by nav.currentBackStackEntryAsState()
                FonecheckTheme {
                    FonecheckNavHost(nav, appPreferences = AppPreferences(onboardingComplete = true))
                }
                val current = entry
                if (current != null &&
                    (current.destination.hasRoute<RunAllTests>() || current.destination.hasRoute<CategoryRetest>())
                ) {
                    val session = hiltViewModel<RunAllTestsViewModel>(current)
                    SideEffect { sessions[current.id] = session }
                }
                val version = renderVersion
                SideEffect {
                    renderedVersion = version
                    navController = nav
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { navController.navigate(RunAllTests) }
        val originalEntry = resumedEntry()
        val originalSession = sessions.getValue(originalEntry)
        // Seed the source via the real session API. Hardware truth is not under test.
        composeRule.runOnIdle {
            originalSession.onPreflightAccepted(RunAllSelections(false, false, false, false), RunAllHardwareProfile())
            originalSession.onPermissionsResolved(RunAllPermissions())
            originalSession.claimStage(originalSession.state.value.stageToken)
            originalSession.onAutomaticChecksComplete(originalSession.state.value.stageToken)
            while (originalSession.state.value.stage != RunAllStage.RESULTS) {
                originalSession.claimStage(originalSession.state.value.stageToken)
                originalSession.skipStage(originalSession.state.value.stageToken)
            }
            val fixture = retestFixture("source", ReportKind.FULL_CHECK)
            originalSession.completeReport(
                originalSession.state.value.stageToken,
                fixture.device,
                fixture.app,
                fixture.categories.map {
                    DiagnosticCategorySnapshot(DiagnosticSnapshotVersion.CURRENT, it.categoryId, it.evidence)
                },
            )
        }
        val original = awaitSaved(originalSession)
        openRetestTwice()
        val cancelledEntry = assertFreshRetest(originalEntry, originalSession)
        click(R.string.report_retest_cancel)
        assertEquals(originalEntry, resumedEntry())
        assertSame(original, originalSession.state.value.report)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        assertEquals(originalEntry, resumedEntry())
        assertSame(original, originalSession.state.value.report)

        openRetestTwice()
        val bEntry = assertFreshRetest(originalEntry, originalSession)
        assertNotEquals(cancelledEntry, bEntry)
        val bSession = sessions.getValue(bEntry)
        val b = completeButtons(bSession)
        openRetestTwice()
        val cEntry = assertFreshRetest(bEntry, bSession)
        val cSession = sessions.getValue(cEntry)
        val c = completeButtons(cSession)
        assertNotEquals(b.stableId, c.stableId)
        click(R.string.run_all_done)
        assertEquals(bEntry, resumedEntry())
        assertSame(b, bSession.state.value.report)
        click(R.string.run_all_done)
        assertEquals(originalEntry, resumedEntry())
        assertSame(original, originalSession.state.value.report)

        val context = composeRule.activity.applicationContext
        val database = DatabaseModule.provideDatabase(context)
        try {
            val repository = RoomReportRepository(database.reportDao())
            runBlocking {
                for (report in listOf(original, b, c)) {
                    assertEquals(report, (repository.getById(report.stableId) as ReportLoadResult.Available).report)
                }
                val exporter = AndroidReportExporter(context, ReportPdfRenderer(context), Dispatchers.IO)
                val exported = exporter.exportJson(b)
                val payload =
                    context.contentResolver
                        .openInputStream(android.net.Uri.parse(exported.uri))!!
                        .bufferedReader()
                        .use { it.readText() }
                assertEquals(b, ReportPayloadCodec.decode(payload))
            }
            // Reopen through the production saved-report route, then cancel back to it.
            composeRule.runOnIdle { navController.navigate(Report(original.stableId)) }
            val savedEntry = resumedEntry()
            openRetestTwice()
            assertFreshRetest(savedEntry, originalSession)
            click(R.string.report_retest_cancel)
            assertEquals(savedEntry, resumedEntry())
            runBlocking {
                assertEquals(original, (repository.getById(original.stableId) as ReportLoadResult.Available).report)
            }
            assertEquals(setOf(original.stableId, b.stableId, c.stableId), savedReportIds() - initialReportIds)
        } finally {
            database.close()
        }
    }

    private fun assertFreshRetest(
        sourceEntry: String,
        sourceSession: RunAllTestsViewModel,
    ): String {
        val entryId = resumedEntry()
        assertNotEquals(sourceEntry, entryId)
        composeRule.runOnIdle {
            assertEquals(CategoryRetest("buttons"), navController.currentBackStackEntry!!.toRoute<CategoryRetest>())
            assertEquals(sourceEntry, navController.previousBackStackEntry!!.id)
            assertEquals(RunAllTestsState(), sessions.getValue(entryId).state.value)
            assertNotSame(sourceSession, sessions.getValue(entryId))
        }
        return entryId
    }

    private fun openRetestTwice() {
        val node = composeRule.scrollToReportText(text(R.string.report_retest))
        val click = node.fetchSemanticsNode().config[SemanticsActions.OnClick].action!!
        composeRule.runOnIdle {
            click()
            click()
            renderVersion += 1
        }
        composeRule.waitForIdle()
        assertEquals(renderVersion, renderedVersion)
    }

    private fun completeButtons(session: RunAllTestsViewModel): DiagnosticReport {
        click(R.string.report_retest_start)
        composeRule.waitUntil(15_000L) { session.state.value.stage == RunAllStage.BUTTONS }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            composeRule.activity.volumeButtonEventSource.record(VolumeButtonDirection.UP)
            composeRule.activity.volumeButtonEventSource.record(VolumeButtonDirection.DOWN)
        }
        val report = awaitSaved(session)
        assertEquals(ReportKind.CATEGORY_ONLY, report.kind)
        assertEquals(listOf(DiagnosticCategoryId.BUTTONS), report.categories.map { it.categoryId })
        assertTrue(
            report.categories
                .single()
                .evidence
                .any { it.checkId.value == "buttons.power" },
        )
        // Successful volume evidence is collapsed by default.
        composeRule
            .scrollToReportText(
                text(R.string.home_cat_buttons),
                ignoreCase = true,
            ).performScrollTo()
            .performClick()
        return report
    }

    private fun awaitSaved(session: RunAllTestsViewModel): DiagnosticReport {
        composeRule.waitUntil(15_000L) { session.state.value.saveStatus == ReportSaveStatus.SAVED }
        composeRule.waitForIdle()
        return requireNotNull(session.state.value.report)
    }

    private fun resumedEntry(): String {
        composeRule.waitUntil(15_000L) {
            navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED &&
                (
                    navController.currentBackStackEntry?.destination?.hasRoute<Report>() == true ||
                        sessions.containsKey(navController.currentBackStackEntry?.id)
                )
        }
        return composeRule.runOnIdle { navController.currentBackStackEntry!!.id }
    }

    private fun click(resource: Int) {
        if (resource == R.string.run_all_done) {
            composeRule.scrollToReportText(text(resource)).performClick()
        } else {
            composeRule.onNodeWithText(text(resource)).performScrollTo().performClick()
        }
    }

    private fun text(resource: Int): String = composeRule.activity.getString(resource)

    private fun savedReportIds(): Set<String> {
        val database = DatabaseModule.provideDatabase(composeRule.activity.applicationContext)
        return try {
            runBlocking {
                RoomReportRepository(database.reportDao())
                    .observeSummaries()
                    .first()
                    .map { it.stableId }
                    .toSet()
            }
        } finally {
            database.close()
        }
    }
}
