package com.insaner.fonecheck.journey

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.local.FonecheckDatabase
import com.insaner.fonecheck.data.repository.RoomReportRepository
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.ui.MainActivity
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoProvider
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoViewModel
import com.insaner.fonecheck.ui.screens.performance.PerformanceBenchmarkRunner
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoProvider
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoViewModel
import com.insaner.fonecheck.ui.screens.runall.ReportSaveStatus
import com.insaner.fonecheck.ui.screens.runall.RunAllInterruptionReason
import com.insaner.fonecheck.ui.screens.runall.RunAllStage
import com.insaner.fonecheck.ui.screens.runall.RunAllStageOutcome
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsScreen
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsViewModel
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyProvider
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyViewModel
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkResult
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkRunner
import com.insaner.fonecheck.ui.screens.storage.StorageInfo
import com.insaner.fonecheck.ui.screens.storage.StorageInfoProvider
import com.insaner.fonecheck.ui.screens.storage.StorageTestViewModel
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Emulator-only synthetic readings. Uses the production screen, executor and category ViewModels. */
@RunWith(AndroidJUnit4::class)
class JourneyAutomaticStageTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun fullCheckUiResolvesPermissionsStartsAutomaticWorkAndAdvances() =
        withHost { host ->
            host.startFullCheckThroughUi()
            host.assertStarted()
            host.release.complete(Unit)
            compose.waitUntil(10_000L) { host.run.state.value.stage == RunAllStage.DISPLAY }
            assertEquals(RunAllStageOutcome.COMPLETED, host.run.state.value.stageOutcomes[RunAllStage.AUTOMATIC])
            assertEquals(1, host.starts.get())
            assertEquals(host.result, host.storage.state.value.benchmarkResult)
            assertNull(host.run.state.value.report)
        }

    @Test
    fun backgroundAfterAutomaticStartupStopsWorkAndRejectsLateCompletion() =
        withHost { host ->
            host.startFullCheckThroughUi()
            host.assertStarted()
            host.scenario.moveToState(Lifecycle.State.CREATED)
            assertTrue(host.cancelled.await(10, TimeUnit.SECONDS))
            assertEquals(RunAllStage.PREFLIGHT, host.run.state.value.stage)
            assertEquals(RunAllInterruptionReason.BACKGROUND, host.run.state.value.lastInterruption)
            host.release.complete(Unit)
            assertTrue(host.returned.await(10, TimeUnit.SECONDS))
            host.scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitForIdle()
            assertEquals(RunAllStage.PREFLIGHT, host.run.state.value.stage)
            assertTrue(
                host.run.state.value.automaticIssues
                    .isEmpty(),
            )
            assertTrue(
                host.run.state.value.stageOutcomes
                    .isEmpty(),
            )
            assertNull(host.storage.state.value.benchmarkResult)
            assertNull(host.run.state.value.report)
            assertEquals(1, host.starts.get())
        }

    @Test
    fun storageRetestUiUsesAutomaticPermissionBypassAndFreezesSyntheticObservation() =
        withHost(DiagnosticCategoryId.STORAGE) { host ->
            host.click(R.string.report_retest_start)
            host.assertStarted()
            host.release.complete(Unit)
            compose.waitUntil(10_000L) { host.run.state.value.saveStatus == ReportSaveStatus.SAVED }
            val report = requireNotNull(host.run.state.value.report)
            assertEquals(ReportKind.CATEGORY_ONLY, report.kind)
            assertEquals(listOf(DiagnosticCategoryId.STORAGE), report.categories.map { it.categoryId })
            assertEquals(host.result, host.storage.state.value.benchmarkResult)
            assertTrue(
                report.categories
                    .single()
                    .evidence
                    .any { it.value == EvidenceValue.DoubleValue(123.0) },
            )
            assertEquals(1, host.starts.get())
            host.scenario.moveToState(Lifecycle.State.CREATED)
            host.scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitForIdle()
            assertSame(report, host.run.state.value.report)
        }

    private fun withHost(
        category: DiagnosticCategoryId? = null,
        test: (Host) -> Unit,
    ) {
        val host = Host(category)
        try {
            test(host)
        } finally {
            host.close()
        }
    }

    private inner class Host(
        category: DiagnosticCategoryId?,
    ) {
        private val instrumentation = InstrumentationRegistry.getInstrumentation()
        private val database =
            Room
                .inMemoryDatabaseBuilder(
                    instrumentation.targetContext,
                    FonecheckDatabase::class.java,
                ).build()
        private val store = ViewModelStore()
        val release = CompletableDeferred<Unit>()
        private val started = CountDownLatch(1)
        val cancelled = CountDownLatch(1)
        val returned = CountDownLatch(1)
        val starts = AtomicInteger()
        lateinit var run: RunAllTestsViewModel
        lateinit var storage: StorageTestViewModel
        private val at = Instant.now()
        private val info =
            StorageInfo(512L * 1_048_576, 256L * 1_048_576, 256L * 1_048_576, 50.0, true, emptyList(), at)
        val result = StorageBenchmarkResult(123.0, 234.0, 1024, 1024, 42, 1, 1024, 1024, info.availableBytes, true, at)
        val scenario: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

        init {
            scenario.onActivity { activity ->
                run =
                    RunAllTestsViewModel(
                        EpochMillisClock {
                            System.currentTimeMillis()
                        },
                        IdProvider { "synthetic-${UUID.randomUUID()}" },
                        RoomReportRepository(database.reportDao()),
                    )
                storage =
                    StorageTestViewModel(
                        StorageInfoProvider { info },
                        StorageBenchmarkRunner {
                            starts.incrementAndGet()
                            started.countDown()
                            try {
                                release.await()
                            } finally {
                                if (!release.isCompleted) cancelled.countDown()
                                // Model a platform operation returning after cancellation without accepting that result.
                                withContext(NonCancellable) { release.await() }
                                returned.countDown()
                            }
                            result
                        },
                        Dispatchers.IO,
                    )
                val device =
                    DeviceInfoViewModel(
                        DeviceInfoProvider {
                            DeviceInfo(
                                "synthetic",
                                "synthetic",
                                "synthetic",
                                "synthetic",
                                "17",
                                37,
                                "2026-09-01",
                                "synthetic",
                                "synthetic",
                                "synthetic",
                                "synthetic",
                                "L1",
                                false,
                                false,
                                false,
                                at,
                            )
                        },
                        Dispatchers.IO,
                    )
                val performance =
                    PerformanceInfoViewModel(
                        PerformanceInfoProvider {
                            PerformanceInfo(
                                "synthetic",
                                "synthetic",
                                2,
                                emptyList(),
                                Confidence.HIGH,
                                100L,
                                50L,
                                Confidence.HIGH,
                                "synthetic",
                                "synthetic",
                                "synthetic",
                                false,
                                Confidence.HIGH,
                                at,
                            )
                        },
                        PerformanceBenchmarkRunner {
                            PerformanceBenchmarkResult(
                                100,
                                100.0,
                                100,
                                1,
                                ThermalStatusCode.NONE,
                                ThermalStatusCode.NONE,
                                at,
                            )
                        },
                        Dispatchers.IO,
                    )
                val sim =
                    SimTelephonyViewModel(
                        SimTelephonyProvider {
                            SimTelephonyInfo(
                                TelephonyHardwareCode.NO_HARDWARE,
                                SimInventoryCode.NO_TELEPHONY,
                                emptyList(),
                                PhoneTypeCode.NONE,
                                0,
                                NetworkGenerationCode.UNKNOWN,
                                false,
                            )
                        },
                        Dispatchers.IO,
                    )
                listOf(run, storage, device, performance, sim).forEachIndexed {
                    index,
                    vm,
                    ->
                    store.put("synthetic-$index", vm)
                }
                activity.setContent {
                    FonecheckTheme {
                        RunAllTestsScreen(
                            {},
                            {},
                            targetCategory = category,
                            showTestWarnings = false,
                            sessionViewModel = run,
                            deviceViewModel = device,
                            performanceViewModel = performance,
                            simViewModel = sim,
                            storageViewModel = storage,
                        )
                    }
                }
            }
            compose.waitForIdle()
        }

        fun click(resource: Int) {
            var text = ""
            scenario.onActivity { text = it.getString(resource) }
            compose.onNodeWithText(text).performScrollTo().performClick()
        }

        fun startFullCheckThroughUi() {
            click(R.string.run_all_preflight_speaker_option)
            click(R.string.run_all_preflight_microphone_option)
            click(R.string.run_all_preflight_camera_option)
            click(R.string.run_all_preflight_start)
            compose.waitForIdle()
            assertEquals(RunAllStage.PERMISSIONS, run.state.value.stage)
            assertEquals(0, starts.get())
            // The real permission review's continue path accepts current permissions without granting any.
            click(R.string.run_all_permissions_continue)
        }

        fun assertStarted() {
            compose.waitUntil(10_000L) { started.count == 0L }
            compose.waitForIdle()
            assertEquals(RunAllStage.AUTOMATIC, run.state.value.stage)
            assertEquals(StorageBenchmarkPhase.RUNNING, storage.state.value.benchmarkPhase)
            assertEquals(1, starts.get())
            scenario.onActivity { assertFalse(run.claimStage(run.state.value.stageToken)) }
        }

        fun close() {
            release.complete(Unit)
            scenario.close()
            instrumentation.runOnMainSync { store.clear() }
            database.close()
        }
    }
}
