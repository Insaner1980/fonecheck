package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.navigation.CategoryRetest
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class ReportRetestActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun freshReportsRequireTheirOwnConfirmedSave() {
        var report by mutableStateOf(retestFixture("full", ReportKind.FULL_CHECK))
        var status by mutableStateOf(ReportSaveStatus.IDLE)
        val routes = mutableListOf<Any>()
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList("en"))) {
                FonecheckTheme {
                    RunAllResultsScreen(report, status, {}, routes::add, {})
                }
            }
        }
        for (kind in ReportKind.entries) {
            composeRule.runOnIdle {
                report = retestFixture(kind.name, kind)
                status = ReportSaveStatus.IDLE
            }
            for (blocked in listOf(ReportSaveStatus.IDLE, ReportSaveStatus.SAVING, ReportSaveStatus.FAILED)) {
                composeRule.runOnIdle { status = blocked }
                composeRule
                    .scrollToReportText("Retest and save")
                    .performScrollTo()
                    .assertIsNotEnabled()
                    .performClick()
                assertEquals(if (kind == ReportKind.FULL_CHECK) 0 else 1, routes.size)
                val message =
                    if (blocked == ReportSaveStatus.FAILED) {
                        "The completed report could not be saved."
                    } else {
                        "Saving the completed report on this device"
                    }
                // The shared save section remains present; there is no live-test fallback.
                composeRule.scrollToReportText(message, substring = true).assertIsDisplayed()
                composeRule.onNodeWithText("Open individual test").assertDoesNotExist()
            }
            composeRule.runOnIdle { status = ReportSaveStatus.SAVED }
            composeRule
                .scrollToReportText("Retest and save")
                .performScrollTo()
                .assertIsEnabled()
                .performClick()
            assertEquals(CategoryRetest("buttons"), routes.last())
        }
        assertEquals(2, routes.size)
    }

    @Test
    fun loadedReportDoesNotDependOnActiveRunSaveStatus() {
        var route: Any? = null
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList("en"))) {
                FonecheckTheme {
                    RunAllResultsScreen(
                        report = retestFixture("saved", ReportKind.CATEGORY_ONLY),
                        saveStatus = ReportSaveStatus.IDLE,
                        onRetrySave = {},
                        onOpenCategory = { route = it },
                        onDone = {},
                        mode = ReportResultMode.SAVED_REPORT,
                    )
                }
            }
        }
        composeRule
            .scrollToReportText("Retest and save")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(CategoryRetest("buttons"), route)
    }

    @Test
    fun localizedActionWrapsAtDoubleFontScale() {
        var language by mutableStateOf("en")
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList(language))
                    .then(DeviceConfigurationOverride.FontScale(2f))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(320.dp, 800.dp))),
            ) {
                FonecheckTheme {
                    RunAllResultsScreen(
                        retestFixture("large", ReportKind.CATEGORY_ONLY),
                        ReportSaveStatus.SAVED,
                        {},
                        {},
                        {},
                        modifier = Modifier,
                    )
                }
            }
        }
        for ((locale, label) in listOf("en" to "Retest and save", "fi" to "Testaa uudelleen ja tallenna")) {
            composeRule.runOnIdle { language = locale }
            composeRule
                .scrollToReportText(label)
                .performScrollTo()
                .assertIsEnabled()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            val layouts = mutableListOf<TextLayoutResult>()
            composeRule
                .onNodeWithText(label, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.isNotEmpty())
            val layout = layouts.single()
            assertFalse("$locale: text must fit vertically", layout.didOverflowHeight)
            // The paragraph retains the available width even when Text measures to its
            // shorter content. Check actual lines, not paragraph width versus measured width.
            for (line in 0 until layout.lineCount) {
                assertFalse("$locale: line $line must not be ellipsised", layout.isLineEllipsized(line))
                assertTrue("$locale: line $line must fit", layout.getLineRight(line) <= layout.size.width + 0.5f)
            }
            assertEquals(label.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true))
            if (locale == "fi") assertTrue(layouts.single().lineCount > 1)
        }
    }
}

internal fun ComposeContentTestRule.scrollToReportText(
    text: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
): SemanticsNodeInteraction {
    onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text, substring, ignoreCase))
    return onNodeWithText(text, substring, ignoreCase)
}

internal fun retestFixture(
    id: String,
    kind: ReportKind,
): DiagnosticReport {
    val capturedAt = Instant.parse("2026-09-01T12:00:00Z")
    val categories =
        if (kind ==
            ReportKind.FULL_CHECK
        ) {
            DiagnosticCatalog.categories
        } else {
            listOf(DiagnosticCategoryId.BUTTONS)
        }
    return ReportAssembler.assemble(
        ReportAssemblyRequest(
            stableId = id,
            kind = kind,
            startedAt = capturedAt.minusSeconds(1),
            completedAt = capturedAt,
            device = ReportDeviceContext("Test", "Emulator", "Test", "Test", "16", 36, null),
            app = ReportAppContext("1.0", 1L),
            snapshots =
                categories.map { category ->
                    DiagnosticCategorySnapshot(
                        DiagnosticSnapshotVersion.CURRENT,
                        category,
                        listOf(
                            DiagnosticEvidence(
                                category,
                                DiagnosticCheckId(category, "${category.stableId}.volume"),
                                if (category ==
                                    DiagnosticCategoryId.BUTTONS
                                ) {
                                    DiagnosticStatus.WARNING
                                } else {
                                    DiagnosticStatus.INFO
                                },
                                Confidence.LOW,
                                EvidenceSource.AUTOMATIC_MEASUREMENT,
                                Applicability.APPLICABLE,
                                reason = EvidenceReasonCode.TIMEOUT,
                                capturedAt = capturedAt,
                            ),
                        ),
                    )
                },
        ),
    )
}
