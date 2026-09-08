package com.insaner.fonecheck.journey

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.navigation.CategoryRetest
import com.insaner.fonecheck.navigation.RunAllTests
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Synthetic I/O on an API-36 emulator; no physical hardware or user report database. */
@RunWith(AndroidJUnit4::class)
class JourneyConfigurationChangeTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun orientationChangeInterruptsActiveFullCheckAndOldSuccessCannotCompleteNewRun() =
        withHost(category = null, lateError = false) { host ->
            host.startThroughUi()
            host.assertPending(1)
            host.changeConfiguration(rotate = true)
            host.assertInterrupted()
            host.startThroughUi()
            host.assertPending(2)
            host.releaseOld.complete(Unit)
            host.awaitOldReturn()
            host.assertPending(2)
            assertNull(host.storage.state.value.benchmarkResult)
            assertTrue(
                host.run.state.value.automaticIssues
                    .isEmpty(),
            )
            host.releaseNew.complete(Unit)
            compose.waitUntil(10_000L) { host.run.state.value.stage == RunAllStage.DISPLAY }
            assertEquals(host.newResult, host.storage.state.value.benchmarkResult)
            assertEquals(RunAllStageOutcome.COMPLETED, host.run.state.value.stageOutcomes[RunAllStage.AUTOMATIC])
            assertNull(host.run.state.value.report)
            host.assertNoSavedReports()
        }

    @Test
    fun explicitRecreationRetainsNavigationOwnersAndOldErrorCannotMutateNewFrozenReport() =
        withHost(category = DiagnosticCategoryId.STORAGE, lateError = true) { host ->
            host.startThroughUi()
            host.assertPending(1)
            host.changeConfiguration(rotate = false)
            host.assertInterrupted()
            host.startThroughUi()
            host.assertPending(2)
            host.releaseNew.complete(Unit)
            compose.waitUntil(10_000L) { host.run.state.value.saveStatus == ReportSaveStatus.SAVED }
            val frozen = requireNotNull(host.run.state.value.report)
            assertEquals(listOf(DiagnosticCategoryId.STORAGE), frozen.categories.map { it.categoryId })
            assertEquals(
                EvidenceValue.DoubleValue(234.0),
                frozen.categories
                    .single()
                    .evidence
                    .single {
                        it.checkId.value == "storage.sequential_write"
                    }.value,
            )
            host.releaseOld.complete(Unit)
            host.awaitOldReturn()
            assertSame(frozen, host.run.state.value.report)
            assertEquals(host.newResult, host.storage.state.value.benchmarkResult)
            assertNull(host.storage.state.value.benchmarkError)
            assertTrue(
                host.run.state.value.automaticIssues
                    .isEmpty(),
            )
            assertEquals(2, host.starts.get())
            assertEquals(
                listOf(frozen.stableId),
                runBlocking {
                    host.repository
                        .observeSummaries()
                        .first()
                        .map { it.stableId }
                },
            )
        }

    private fun withHost(
        category: DiagnosticCategoryId?,
        lateError: Boolean,
        test: (Host) -> Unit,
    ) {
        val host = Host(category, lateError)
        try {
            host.launch()
            test(host)
        } finally {
            host.close()
        }
    }

    private inner class Host(
        private val category: DiagnosticCategoryId?,
        private val lateError: Boolean,
    ) {
        private val instrumentation = InstrumentationRegistry.getInstrumentation()
        private val database =
            Room
                .inMemoryDatabaseBuilder(
                    instrumentation.targetContext,
                    FonecheckDatabase::class.java,
                ).build()
        val repository = RoomReportRepository(database.reportDao())
        val releaseOld = CompletableDeferred<Unit>()
        val releaseNew = CompletableDeferred<Unit>()
        private val cancelled = CountDownLatch(1)
        private val oldReturned = CountDownLatch(1)
        private lateinit var oldOperation: Job
        val starts = AtomicInteger()
        private val createdRun = AtomicInteger()
        private val createdStorage = AtomicInteger()
        lateinit var run: RunAllTestsViewModel
        lateinit var storage: StorageTestViewModel
        private lateinit var entryId: String
        private var scenario: ActivityScenario<ConfigurationJourneyActivity>? = null
        private var originalRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        private val at = Instant.now()
        private val info =
            StorageInfo(512L * 1_048_576, 256L * 1_048_576, 256L * 1_048_576, 50.0, true, emptyList(), at)
        private val oldResult =
            StorageBenchmarkResult(12.0, 23.0, 1024, 1024, 42, 1, 1024, 1024, info.availableBytes, true, at)
        val newResult = oldResult.copy(writeMebibytesPerSecond = 234.0, readMebibytesPerSecond = 345.0)

        private val factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val model =
                        when (modelClass) {
                            RunAllTestsViewModel::class.java -> {
                                createdRun.incrementAndGet()
                                RunAllTestsViewModel(
                                    EpochMillisClock {
                                        System.currentTimeMillis()
                                    },
                                    IdProvider { "synthetic-${UUID.randomUUID()}" },
                                    repository,
                                )
                            }
                            StorageTestViewModel::class.java -> {
                                createdStorage.incrementAndGet()
                                StorageTestViewModel(
                                    StorageInfoProvider { info },
                                    StorageBenchmarkRunner {
                                        when (starts.incrementAndGet()) {
                                            1 -> {
                                                try {
                                                    releaseOld.await()
                                                } catch (_: CancellationException) {
                                                    cancelled.countDown()
                                                    // Model blocking I/O returning a value/error after its caller cancelled.
                                                    withContext(NonCancellable) { releaseOld.await() }
                                                }
                                                oldReturned.countDown()
                                                if (lateError) throw IOException("synthetic obsolete I/O failure")
                                                oldResult
                                            }
                                            2 -> {
                                                releaseNew.await()
                                                newResult
                                            }
                                            else -> error("Unexpected duplicate benchmark start")
                                        }
                                    },
                                    Dispatchers.IO,
                                )
                            }
                            DeviceInfoViewModel::class.java ->
                                DeviceInfoViewModel(
                                    DeviceInfoProvider {
                                        DeviceInfo(
                                            "synthetic",
                                            "synthetic",
                                            "synthetic",
                                            "synthetic",
                                            "16",
                                            36,
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
                            PerformanceInfoViewModel::class.java ->
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
                            SimTelephonyViewModel::class.java ->
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
                            else -> error("Unexpected ViewModel: $modelClass")
                        }
                    return modelClass.cast(model)!!
                }
            }

        fun launch() {
            val context = instrumentation.targetContext
            val production = context.packageManager.getActivityInfo(ComponentName(context, MainActivity::class.java), 0)
            val test =
                context.packageManager.getActivityInfo(
                    ComponentName(context, ConfigurationJourneyActivity::class.java),
                    0,
                )
            assertEquals(production.configChanges, test.configChanges)
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, production.screenOrientation)
            assertEquals(production.screenOrientation, test.screenOrientation)
            assertEquals(0, production.configChanges and ActivityInfo.CONFIG_ORIENTATION)
            ConfigurationJourneyActivity.content = {
                val nav = rememberNavController()
                FonecheckTheme {
                    NavHost(
                        navController = nav,
                        startDestination =
                            category?.let { CategoryRetest(it.stableId) } ?: RunAllTests,
                    ) {
                        composable<RunAllTests> { entry -> RunContent(entry) }
                        composable<CategoryRetest> { entry -> RunContent(entry) }
                    }
                }
            }
            scenario = ActivityScenario.launch(ConfigurationJourneyActivity::class.java)
            requireNotNull(scenario).onActivity { originalRequestedOrientation = it.requestedOrientation }
            compose.waitForIdle()
        }

        @Composable
        private fun RunContent(entry: NavBackStackEntry) {
            // Exactly the production destination ownership: the NavBackStackEntry, not the Activity or test.
            val session: RunAllTestsViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
            val storageModel: StorageTestViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
            val device: DeviceInfoViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
            val performance: PerformanceInfoViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
            val sim: SimTelephonyViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
            RunAllTestsScreen(
                {},
                {},
                targetCategory = category,
                showTestWarnings = false,
                sessionViewModel = session,
                storageViewModel = storageModel,
                deviceViewModel = device,
                performanceViewModel = performance,
                simViewModel = sim,
            )
            SideEffect {
                run = session
                storage = storageModel
                entryId = entry.id
            }
        }

        fun startThroughUi() {
            if (category == null) {
                click(R.string.run_all_preflight_speaker_option)
                click(R.string.run_all_preflight_microphone_option)
                click(R.string.run_all_preflight_camera_option)
                click(R.string.run_all_preflight_start)
                compose.waitForIdle()
                assertEquals(RunAllStage.PERMISSIONS, run.state.value.stage)
                click(R.string.run_all_permissions_continue)
            } else {
                click(R.string.report_retest_start)
            }
        }

        fun assertPending(expectedStarts: Int) {
            compose.waitUntil(10_000L) { starts.get() == expectedStarts }
            compose.waitForIdle()
            assertEquals(RunAllStage.AUTOMATIC, run.state.value.stage)
            assertEquals(StorageBenchmarkPhase.RUNNING, storage.state.value.benchmarkPhase)
            requireNotNull(scenario).onActivity {
                assertFalse(run.claimStage(run.state.value.stageToken))
                if (expectedStarts ==
                    1
                ) {
                    oldOperation =
                        storage.viewModelScope.coroutineContext[Job]!!
                            .children
                            .single { it.isActive }
                }
            }
        }

        fun changeConfiguration(rotate: Boolean) {
            val host = requireNotNull(scenario)
            lateinit var oldActivity: ConfigurationJourneyActivity
            val oldRun = run
            val oldStorage = storage
            val oldEntryId = entryId
            val oldToken = run.state.value.stageToken
            var oldOrientation = 0
            var stoppedForConfiguration = false
            host.onActivity { activity ->
                oldActivity = activity
                oldOrientation = activity.resources.configuration.orientation
                activity.lifecycle.addObserver(
                    LifecycleEventObserver { _, event ->
                        if (event ==
                            Lifecycle.Event.ON_STOP
                        ) {
                            stoppedForConfiguration = activity.isChangingConfigurations
                        }
                    },
                )
                if (rotate) {
                    activity.requestedOrientation =
                        if (oldOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                }
            }
            if (!rotate) host.recreate()
            compose.waitUntil(10_000L) {
                var recreated = false
                host.onActivity { recreated = it !== oldActivity }
                recreated && oldActivity.isDestroyed
            }
            compose.waitForIdle()
            host.onActivity { activity ->
                assertNotSame(oldActivity, activity)
                assertTrue(stoppedForConfiguration)
                if (rotate) {
                    assertTrue(oldOrientation != activity.resources.configuration.orientation)
                } else {
                    assertEquals(oldOrientation, activity.resources.configuration.orientation)
                }
                assertSame(oldRun, run)
                assertSame(oldStorage, storage)
                assertEquals(oldEntryId, entryId)
                assertTrue(oldToken != run.state.value.stageToken)
                assertEquals(1, createdRun.get())
                assertEquals(1, createdStorage.get())
                instrumentation.sendStatus(
                    0,
                    Bundle().apply {
                        putString(
                            "stream",
                            "configuration journey rotation=$rotate orientation=$oldOrientation->${activity.resources.configuration.orientation} " +
                                "recreated=true retainedEntry=$entryId retainedViewModels=true reason=${run.state.value.lastInterruption}\n",
                        )
                    },
                )
            }
        }

        fun assertInterrupted() {
            assertTrue(cancelled.await(10, TimeUnit.SECONDS))
            assertEquals(RunAllStage.PREFLIGHT, run.state.value.stage)
            assertEquals(RunAllInterruptionReason.CONFIGURATION_CHANGE, run.state.value.lastInterruption)
            assertEquals(StorageBenchmarkPhase.CANCELLED, storage.state.value.benchmarkPhase)
            assertTrue(
                run.state.value.stageOutcomes
                    .isEmpty(),
            )
            assertTrue(
                run.state.value.automaticIssues
                    .isEmpty(),
            )
            assertNull(run.state.value.report)
            assertEquals(1, starts.get())
            compose.onNodeWithText(text(R.string.run_all_interrupted_configuration)).assertExists()
            assertNoSavedReports()
        }

        fun awaitOldReturn() {
            assertTrue(oldReturned.await(10, TimeUnit.SECONDS))
            compose.waitUntil(10_000L) { oldOperation.isCompleted }
            compose.waitForIdle()
        }

        fun assertNoSavedReports() = assertTrue(runBlocking { repository.observeSummaries().first().isEmpty() })

        private fun text(resource: Int): String {
            var result = ""
            requireNotNull(scenario).onActivity { result = it.getString(resource) }
            return result
        }

        private fun click(resource: Int) = compose.onNodeWithText(text(resource)).performScrollTo().performClick()

        fun close() {
            releaseOld.complete(Unit)
            releaseNew.complete(Unit)
            scenario?.onActivity { it.requestedOrientation = originalRequestedOrientation }
            scenario?.close()
            ConfigurationJourneyActivity.content = null
            database.close()
        }
    }
}
