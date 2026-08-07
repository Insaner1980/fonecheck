package com.insaner.fonecheck.ui.screens.vibration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VibrationTestViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startingANewPatternCancelsTheOwnedVibrationFirst() =
        runTest(dispatcher.scheduler) {
            val platform = FakeVibrationPlatform()
            val viewModel = VibrationTestViewModel(platform)

            viewModel.vibrateLong()
            viewModel.vibratePattern()

            assertEquals(
                listOf("play:LONG", "cancel", "play:PATTERN"),
                platform.events,
            )
            assertTrue(viewModel.state.value.isPlaying)
            assertEquals(VibrationPattern.PATTERN, viewModel.state.value.lastPattern)
        }

    @Test
    fun cancelIsIdempotentAndNaturalCompletionReleasesOwnership() =
        runTest(dispatcher.scheduler) {
            val platform = FakeVibrationPlatform()
            val viewModel = VibrationTestViewModel(platform)

            viewModel.vibrateShort()
            viewModel.cancelVibration()
            viewModel.cancelVibration()

            assertEquals(1, platform.cancelCount)
            assertFalse(viewModel.state.value.isPlaying)

            viewModel.vibrateShort()
            advanceTimeBy(101L)
            runCurrent()
            assertFalse(viewModel.state.value.isPlaying)
            viewModel.cancelVibration()
            assertEquals(1, platform.cancelCount)
        }

    @Test
    fun noVibratorDoesNotStartAnEffectAndCanBeSkipped() =
        runTest(dispatcher.scheduler) {
            val platform =
                FakeVibrationPlatform(
                    capabilities = HapticCapabilityState(hasVibrator = false),
                )
            val viewModel = VibrationTestViewModel(platform)

            viewModel.vibratePattern()
            viewModel.skipMotorConfirmation()

            assertTrue(platform.events.isEmpty())
            assertFalse(viewModel.state.value.isPlaying)
            assertEquals(VibrationMotorResult.SKIPPED, viewModel.state.value.motor.result)
        }

    @Test
    fun apiCapabilitiesRemainSeparateFromPhysicalConfirmation() =
        runTest(dispatcher.scheduler) {
            val capabilities =
                HapticCapabilityState(
                    hasVibrator = true,
                    hasAmplitudeControl = true,
                    effectsApiSupported = true,
                    supportedEffects = listOf(VibrationEffectCode.CLICK),
                    primitivesApiSupported = true,
                    supportedPrimitives = listOf(VibrationPrimitiveCode.THUD),
                )
            val viewModel = VibrationTestViewModel(FakeVibrationPlatform(capabilities))

            viewModel.reportFelt(false)

            assertEquals(capabilities, viewModel.state.value.haptic)
            assertEquals(VibrationMotorResult.NOT_FELT, viewModel.state.value.motor.result)
        }

    @Test
    fun clearingViewModelCancelsAnOwnedVibration() =
        runTest(dispatcher.scheduler) {
            val platform = FakeVibrationPlatform()
            val viewModel = VibrationTestViewModel(platform)
            viewModel.vibrateLong()

            VibrationTestViewModel::class.java
                .getDeclaredMethod("onCleared")
                .apply { isAccessible = true }
                .invoke(viewModel)

            assertEquals(1, platform.cancelCount)
            assertFalse(viewModel.state.value.isPlaying)
        }

    private class FakeVibrationPlatform(
        override val capabilities: HapticCapabilityState =
            HapticCapabilityState(
                hasVibrator = true,
                hasAmplitudeControl = true,
                effectsApiSupported = true,
                supportedEffects = listOf(VibrationEffectCode.CLICK),
                primitivesApiSupported = true,
                supportedPrimitives = listOf(VibrationPrimitiveCode.CLICK),
            ),
    ) : VibrationPlatform {
        val events = mutableListOf<String>()
        var cancelCount = 0

        override fun play(pattern: VibrationPattern) {
            events += "play:${pattern.name}"
        }

        override fun cancel() {
            events += "cancel"
            cancelCount += 1
        }
    }
}
