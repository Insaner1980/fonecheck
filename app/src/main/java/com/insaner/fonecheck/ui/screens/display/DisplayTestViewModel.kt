package com.insaner.fonecheck.ui.screens.display

import android.app.Application
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.provider.Settings
import android.view.Display
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class DisplayResolutionSource {
    APP_WINDOW,
    DISPLAY_MODE,
    PHYSICAL_METRICS,
}

data class DisplayInfoState(
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val resolutionSource: DisplayResolutionSource = DisplayResolutionSource.APP_WINDOW,
    val densityDpi: Int = 0,
    val refreshRate: Float = 0f,
    val hdrSupported: Boolean = false,
    val wideColorGamut: Boolean = false,
    val currentBrightness: Int = 0,
    val autoBrightness: Boolean = false,
)

data class TouchTestState(
    val isActive: Boolean = false,
    val touchedCells: Set<Int> = emptySet(),
    val activePointers: Map<Long, TouchPoint> = emptyMap(),
    val maxPointerCount: Int = 0,
    val isComplete: Boolean = false,
)

data class VisualTestState(
    val isActive: Boolean = false,
    val patternIndex: Int = 0,
    val result: Boolean? = null,
)

enum class DisplaySection {
    INFO,
    VISUAL,
    TOUCH,
}

data class DisplayTestState(
    val info: DisplayInfoState = DisplayInfoState(),
    val touch: TouchTestState = TouchTestState(),
    val visual: VisualTestState = VisualTestState(),
    val expandedSection: DisplaySection? = null,
)

@HiltViewModel
class DisplayTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()

        private val _state = MutableStateFlow(DisplayTestState())
        val state: StateFlow<DisplayTestState> = _state.asStateFlow()

        init {
            loadDisplayInfo()
        }

        fun toggleSection(section: DisplaySection) {
            val current = _state.value.expandedSection
            _state.value = _state.value.copy(expandedSection = if (current == section) null else section)
        }

        fun startVisualTest() {
            _state.value =
                _state.value.copy(
                    visual = VisualTestState(isActive = true),
                )
        }

        fun nextVisualPattern() {
            val current = _state.value.visual.patternIndex
            if (current < DisplayPattern.entries.lastIndex) {
                _state.value =
                    _state.value.copy(
                        visual = _state.value.visual.copy(patternIndex = current + 1),
                    )
            }
        }

        fun previousVisualPattern() {
            val current = _state.value.visual.patternIndex
            if (current > 0) {
                _state.value =
                    _state.value.copy(
                        visual = _state.value.visual.copy(patternIndex = current - 1),
                    )
            }
        }

        fun completeVisualTest(passed: Boolean) {
            _state.value =
                _state.value.copy(
                    visual = _state.value.visual.copy(isActive = false, result = passed),
                )
        }

        fun stopVisualTest() {
            _state.value =
                _state.value.copy(
                    visual = _state.value.visual.copy(isActive = false),
                )
        }

        fun startTouchTest() {
            _state.value =
                _state.value.copy(
                    touch = TouchTestState(isActive = true),
                )
        }

        fun recordTouch(
            cells: Set<Int> = emptySet(),
            activePointers: Map<Long, TouchPoint> = _state.value.touch.activePointers,
        ) {
            _state.value =
                _state.value.copy(
                    touch = TouchTestReducer.record(_state.value.touch, cells, activePointers),
                )
        }

        fun resetTouchTest() {
            _state.value = _state.value.copy(touch = TouchTestReducer.reset(_state.value.touch))
        }

        fun completeTouchTest() {
            _state.value = _state.value.copy(touch = TouchTestReducer.complete(_state.value.touch))
        }

        fun stopTouchTest() {
            _state.value =
                _state.value.copy(
                    touch = _state.value.touch.copy(isActive = false, activePointers = emptyMap()),
                )
        }

        private fun loadDisplayInfo() {
            val windowManager = context.getSystemService(WindowManager::class.java)
            val display =
                context
                    .getSystemService(DisplayManager::class.java)
                    .getDisplay(Display.DEFAULT_DISPLAY)
            val resolution = readResolution(windowManager, display)
            val brightness =
                runCatching {
                    Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                }.getOrDefault(0)
            val autoBrightness =
                runCatching {
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                    ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                }.getOrDefault(false)

            _state.value =
                _state.value.copy(
                    info =
                        DisplayInfoState(
                            widthPx = resolution.width,
                            heightPx = resolution.height,
                            resolutionSource = resolution.source,
                            densityDpi = context.resources.displayMetrics.densityDpi,
                            refreshRate = display?.refreshRate ?: 0f,
                            hdrSupported = display?.isHdr == true,
                            wideColorGamut = display?.isWideColorGamut == true,
                            currentBrightness = brightness,
                            autoBrightness = autoBrightness,
                        ),
                )
        }

        private fun readResolution(
            windowManager: WindowManager,
            display: Display?,
        ): ResolutionReading {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                return ResolutionReading(
                    width = bounds.width(),
                    height = bounds.height(),
                    source = DisplayResolutionSource.APP_WINDOW,
                )
            }
            val mode = display?.mode
            if (mode != null && mode.physicalWidth > 0 && mode.physicalHeight > 0) {
                return ResolutionReading(
                    width = mode.physicalWidth,
                    height = mode.physicalHeight,
                    source = DisplayResolutionSource.DISPLAY_MODE,
                )
            }
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            display?.getRealMetrics(metrics)
            val fallback = context.resources.displayMetrics
            return ResolutionReading(
                width = metrics.widthPixels.takeIf { it > 0 } ?: fallback.widthPixels,
                height = metrics.heightPixels.takeIf { it > 0 } ?: fallback.heightPixels,
                source = DisplayResolutionSource.PHYSICAL_METRICS,
            )
        }

        private data class ResolutionReading(
            val width: Int,
            val height: Int,
            val source: DisplayResolutionSource,
        )

        companion object {
            const val TOUCH_GRID_COLS = 6
            const val TOUCH_GRID_ROWS = 10
            const val VISUAL_TEST_TIMEOUT_MS = 120_000L
        }
    }
