package com.insaner.fonecheck.journey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.ui.MainActivity
import com.insaner.fonecheck.ui.screens.runall.RunAllHardwareProfile
import com.insaner.fonecheck.ui.screens.runall.RunAllInterruptionReason
import com.insaner.fonecheck.ui.screens.runall.RunAllStage
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsScreen
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsViewModel
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Exercises the production interruption observer before permissions are resolved: no diagnostic starts. */
@RunWith(AndroidJUnit4::class)
class JourneyLifecycleTest {
    @Test
    fun backgroundSettingsAndRecreationInterruptWithoutSavingOrResuming() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val events = CopyOnWriteArrayList<Lifecycle.Event>()
        val composed = CountDownLatch(1)
        lateinit var run: RunAllTestsViewModel
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                run = ViewModelProvider(activity)[RunAllTestsViewModel::class.java]
                activity.lifecycle.addObserver(LifecycleEventObserver { _, event -> events.add(event) })
                activity.setContent {
                    FonecheckTheme {
                        RunAllTestsScreen({}, {}, targetCategory = DiagnosticCategoryId.CAMERA, sessionViewModel = run)
                        SideEffect { composed.countDown() }
                    }
                }
            }
            assertTrue(composed.await(10, TimeUnit.SECONDS))
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                run.onCategoryRetestRequested(
                    DiagnosticCategoryId.CAMERA,
                    RunAllHardwareProfile(cameraAvailable = true),
                )
            }
            scenario.moveToState(Lifecycle.State.CREATED)
            assertEquals(RunAllStage.PREFLIGHT, run.state.value.stage)
            assertEquals(RunAllInterruptionReason.BACKGROUND, run.state.value.lastInterruption)
            assertNull(run.state.value.report)
            scenario.moveToState(Lifecycle.State.RESUMED)
            val stopped = CountDownLatch(1)
            scenario.onActivity { activity ->
                run.onCategoryRetestRequested(
                    DiagnosticCategoryId.CAMERA,
                    RunAllHardwareProfile(cameraAvailable = true),
                )
                assertNull(run.state.value.lastInterruption)
                activity.lifecycle.addObserver(
                    LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) stopped.countDown()
                    },
                )
                activity.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}")),
                )
            }
            assertTrue(stopped.await(10, TimeUnit.SECONDS))
            instrumentation.waitForIdleSync()
            assertEquals(RunAllStage.PREFLIGHT, run.state.value.stage)
            assertNull(run.state.value.report)
            instrumentation.targetContext.startActivity(
                Intent(instrumentation.targetContext, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
            )
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity {
                assertEquals(RunAllStage.PREFLIGHT, run.state.value.stage)
                run.onCategoryRetestRequested(
                    DiagnosticCategoryId.CAMERA,
                    RunAllHardwareProfile(cameraAvailable = true),
                )
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertSame(run, ViewModelProvider(activity)[RunAllTestsViewModel::class.java])
                assertEquals(RunAllStage.PREFLIGHT, run.state.value.stage)
                assertTrue(run.state.value.lastInterruption != null)
                assertNull(run.state.value.report)
            }
            assertTrue(
                events.containsAll(
                    listOf(Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY),
                ),
            )
            val details =
                "fonecheck lifecycle PID=${Process.myPid()} events=$events " +
                    "reason=${run.state.value.lastInterruption}\n"
            instrumentation.sendStatus(0, Bundle().apply { putString("stream", details) })
        }
    }
}
