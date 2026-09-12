package com.insaner.fonecheck.ui.screens.runall

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.local.FonecheckDatabase
import com.insaner.fonecheck.data.repository.RoomReportRepository
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.ui.MainActivity
import com.insaner.fonecheck.ui.screens.vibration.HapticCapabilityState
import com.insaner.fonecheck.ui.screens.vibration.VibrationPattern
import com.insaner.fonecheck.ui.screens.vibration.VibrationPlatform
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestViewModel
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class RunAllVibrationReplayTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun failedReplayEndsTheStageWithoutRecordingAMotorFailure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, FonecheckDatabase::class.java).build()
        val store = ViewModelStore()
        val plays = AtomicInteger()
        val platform =
            object : VibrationPlatform {
                override val capabilities = HapticCapabilityState(hasVibrator = true)

                override fun play(pattern: VibrationPattern): Boolean = plays.incrementAndGet() <= 2

                override fun cancel() = Unit
            }
        lateinit var run: RunAllTestsViewModel
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                try {
                    scenario.onActivity { activity ->
                        val clock = EpochMillisClock(System::currentTimeMillis)
                        run =
                            RunAllTestsViewModel(
                                clock,
                                IdProvider { "vibration-replay" },
                                RoomReportRepository(database.reportDao()),
                            )
                        val vibration = VibrationTestViewModel(platform, clock)
                        store.put("run", run)
                        store.put("vibration", vibration)
                        activity.setContent {
                            FonecheckTheme {
                                RunAllTestsScreen(
                                    onDone = {},
                                    onOpenCategory = {},
                                    targetCategory = DiagnosticCategoryId.VIBRATION,
                                    sessionViewModel = run,
                                    vibrationViewModel = vibration,
                                )
                            }
                        }
                    }
                    compose
                        .onNodeWithText(context.getString(R.string.report_retest_start))
                        .performScrollTo()
                        .performClick()
                    compose.waitUntil(10_000L) { plays.get() == 1 }
                    val replay = context.getString(R.string.run_all_play_again)
                    compose.onNodeWithText(replay).performScrollTo().performClick()
                    compose.runOnIdle {
                        assertEquals(2, plays.get())
                        assertFalse(run.state.value.awaitingContinue)
                    }
                    val token = run.state.value.stageToken
                    compose.onNodeWithText(replay).performScrollTo().performClick()
                    compose.waitUntil(10_000L) { run.state.value.awaitingContinue }
                    compose.runOnIdle {
                        assertEquals(3, plays.get())
                        assertEquals(
                            RunAllStageOutcome.ERROR,
                            run.state.value.manualChecks.outcomes[RunAllStage.VIBRATION],
                        )
                        run.recordVibration(token, false)
                        assertNull(run.state.value.manualChecks.vibration)
                        assertEquals(
                            RunAllStageOutcome.ERROR,
                            run.state.value.manualChecks.outcomes[RunAllStage.VIBRATION],
                        )
                    }
                    compose.onNodeWithText(replay).assertDoesNotExist()
                } finally {
                    scenario.onActivity { store.clear() }
                }
            }
        } finally {
            database.close()
        }
    }
}
