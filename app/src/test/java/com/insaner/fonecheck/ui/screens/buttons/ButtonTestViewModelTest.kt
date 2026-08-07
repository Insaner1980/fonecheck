package com.insaner.fonecheck.ui.screens.buttons

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
class ButtonTestViewModelTest {
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
    fun directVolumeKeyEventsCompleteTestWithoutDependingOnVolumeLevel() =
        runTest(dispatcher.scheduler) {
            val source = FakeVolumeButtonEventSource()
            val viewModel = ButtonTestViewModel(source)

            viewModel.startTest()
            runCurrent()
            source.record(VolumeButtonDirection.UP)
            source.record(VolumeButtonDirection.DOWN)
            runCurrent()

            assertEquals(ButtonTestPhase.COMPLETED, viewModel.state.value.phase)
            assertTrue(viewModel.state.value.volumeUpDetected)
            assertTrue(viewModel.state.value.volumeDownDetected)
        }

    @Test
    fun externalVolumeChangeWithoutKeyEventTimesOut() =
        runTest(dispatcher.scheduler) {
            val viewModel = ButtonTestViewModel(FakeVolumeButtonEventSource())

            viewModel.startTest()
            advanceTimeBy(ButtonTestViewModel.TEST_TIMEOUT_MILLIS + 1L)
            runCurrent()

            assertEquals(ButtonTestPhase.TIMED_OUT, viewModel.state.value.phase)
            assertFalse(viewModel.state.value.volumeUpDetected)
            assertFalse(viewModel.state.value.volumeDownDetected)
        }

    @Test
    fun retryAfterTimeoutStartsFreshAndCanComplete() =
        runTest(dispatcher.scheduler) {
            val source = FakeVolumeButtonEventSource()
            val viewModel = ButtonTestViewModel(source)
            viewModel.startTest()
            advanceTimeBy(ButtonTestViewModel.TEST_TIMEOUT_MILLIS + 1L)
            runCurrent()

            viewModel.retry()
            runCurrent()
            source.record(VolumeButtonDirection.DOWN)
            source.record(VolumeButtonDirection.UP)
            runCurrent()

            assertEquals(ButtonTestPhase.COMPLETED, viewModel.state.value.phase)
        }

    @Test
    fun stoppingTestCancelsCollectionAndResetClearsPartialResult() =
        runTest(dispatcher.scheduler) {
            val source = FakeVolumeButtonEventSource()
            val viewModel = ButtonTestViewModel(source)
            viewModel.startTest()
            runCurrent()
            source.record(VolumeButtonDirection.UP)
            runCurrent()

            viewModel.stopTest()
            source.record(VolumeButtonDirection.DOWN)
            runCurrent()

            assertEquals(ButtonTestPhase.IDLE, viewModel.state.value.phase)
            assertTrue(viewModel.state.value.volumeUpDetected)
            assertFalse(viewModel.state.value.volumeDownDetected)

            viewModel.reset()
            assertEquals(ButtonTestState(), viewModel.state.value)
        }

    @Test
    fun skipAndClearStopTheActiveTest() =
        runTest(dispatcher.scheduler) {
            val source = FakeVolumeButtonEventSource()
            val viewModel = ButtonTestViewModel(source)
            viewModel.startTest()
            runCurrent()

            viewModel.skip()
            assertEquals(ButtonTestPhase.SKIPPED, viewModel.state.value.phase)

            viewModel.startTest()
            runCurrent()
            ButtonTestViewModel::class.java
                .getDeclaredMethod("onCleared")
                .apply { isAccessible = true }
                .invoke(viewModel)
            source.record(VolumeButtonDirection.UP)
            runCurrent()

            assertEquals(ButtonTestPhase.IDLE, viewModel.state.value.phase)
            assertFalse(viewModel.state.value.volumeUpDetected)
        }

    private class FakeVolumeButtonEventSource : VolumeButtonEventSource {
        private val mutableEvents = MutableSharedFlow<VolumeButtonDirection>(extraBufferCapacity = 2)
        override val events: Flow<VolumeButtonDirection> = mutableEvents

        override fun record(direction: VolumeButtonDirection) {
            mutableEvents.tryEmit(direction)
        }
    }
}
