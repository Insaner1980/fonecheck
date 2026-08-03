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
import javax.inject.Inject

// ── State classes ────────────────────────────────────────────────────────────────

data class DisplayInfoState(
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val densityDpi: Int = 0,
    val refreshRate: Float = 0f,
    val hdrSupported: Boolean = false,
    val wideColorGamut: Boolean = false,
    val currentBrightness: Int = 0,
    val autoBrightness: Boolean = false,
)

data class TouchTestState(
    val touchedCells: Set<Int> = emptySet(),
    val multiTouchCount: Int = 0,
)

data class DeadPixelState(
    val isActive: Boolean = false,
    val colorIndex: Int = 0,
)

data class BurnInState(
    val isActive: Boolean = false,
)

enum class DisplaySection {
    INFO,
    DEAD_PIXEL,
    TOUCH,
    BURN_IN,
}

data class DisplayTestState(
    val info: DisplayInfoState = DisplayInfoState(),
    val touch: TouchTestState = TouchTestState(),
    val deadPixel: DeadPixelState = DeadPixelState(),
    val burnIn: BurnInState = BurnInState(),
    val expandedSection: DisplaySection? = null,
)

// ── ViewModel ────────────────────────────────────────────────────────────────────

@HiltViewModel
class DisplayTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()

        private val _state = MutableStateFlow(DisplayTestState())
        val state: StateFlow<DisplayTestState> = _state

        init {
            loadDisplayInfo()
        }

        fun toggleSection(section: DisplaySection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
        }

        // ── Dead pixel test ──────────────────────────────────────────────────────────

        fun startDeadPixelTest() {
            _state.value =
                _state.value.copy(
                    deadPixel = DeadPixelState(isActive = true, colorIndex = 0),
                )
        }

        fun nextDeadPixelColor() {
            val current = _state.value.deadPixel.colorIndex
            val next = (current + 1) % DEAD_PIXEL_COLORS
            _state.value =
                _state.value.copy(
                    deadPixel = _state.value.deadPixel.copy(colorIndex = next),
                )
        }

        fun stopDeadPixelTest() {
            _state.value =
                _state.value.copy(
                    deadPixel = DeadPixelState(isActive = false, colorIndex = 0),
                )
        }

        // ── Touch test ───────────────────────────────────────────────────────────────

        fun onCellTouched(cellIndex: Int) {
            val cells = _state.value.touch.touchedCells + cellIndex
            _state.value =
                _state.value.copy(
                    touch = _state.value.touch.copy(touchedCells = cells),
                )
        }

        fun updateMultiTouchCount(count: Int) {
            if (count > _state.value.touch.multiTouchCount) {
                _state.value =
                    _state.value.copy(
                        touch = _state.value.touch.copy(multiTouchCount = count),
                    )
            }
        }

        fun resetTouchTest() {
            _state.value =
                _state.value.copy(
                    touch = TouchTestState(),
                )
        }

        // ── Burn-in test ─────────────────────────────────────────────────────────────

        fun startBurnInTest() {
            _state.value = _state.value.copy(burnIn = BurnInState(isActive = true))
        }

        fun stopBurnInTest() {
            _state.value = _state.value.copy(burnIn = BurnInState(isActive = false))
        }

        // ── Display info ─────────────────────────────────────────────────────────────

        private fun loadDisplayInfo() {
            val windowManager = context.getSystemService(WindowManager::class.java)
            val display =
                context
                    .getSystemService(DisplayManager::class.java)
                    .getDisplay(Display.DEFAULT_DISPLAY)

            val widthPx: Int
            val heightPx: Int

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                widthPx = bounds.width()
                heightPx = bounds.height()
            } else {
                val metrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                display?.getRealMetrics(metrics)
                val resourceMetrics = context.resources.displayMetrics
                widthPx = metrics.widthPixels.takeIf { it > 0 } ?: resourceMetrics.widthPixels
                heightPx = metrics.heightPixels.takeIf { it > 0 } ?: resourceMetrics.heightPixels
            }

            val refreshRate = display?.refreshRate ?: 0f
            val hdrSupported = display?.hdrCapabilities?.supportedHdrTypes?.isNotEmpty() == true
            val wideColorGamut = display?.isWideColorGamut == true
            val densityDpi = context.resources.displayMetrics.densityDpi

            val brightness =
                try {
                    Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                } catch (_: Exception) {
                    0
                }

            val autoBrightness =
                try {
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                    ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                } catch (_: Exception) {
                    false
                }

            _state.value =
                _state.value.copy(
                    info =
                        DisplayInfoState(
                            widthPx = widthPx,
                            heightPx = heightPx,
                            densityDpi = densityDpi,
                            refreshRate = refreshRate,
                            hdrSupported = hdrSupported,
                            wideColorGamut = wideColorGamut,
                            currentBrightness = brightness,
                            autoBrightness = autoBrightness,
                        ),
                )
        }

        companion object {
            const val DEAD_PIXEL_COLORS = 5 // Red, Green, Blue, White, Black
            const val TOUCH_GRID_COLS = 6
            const val TOUCH_GRID_ROWS = 10
        }
    }
