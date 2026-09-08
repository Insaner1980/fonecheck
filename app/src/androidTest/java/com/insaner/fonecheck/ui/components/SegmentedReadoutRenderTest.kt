package com.insaner.fonecheck.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.ui.preview.ReadoutSpecimen
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.math.roundToInt

/** Opt-in device capture only. Does not start diagnostics or navigate the production app. */
@RunWith(Parameterized::class)
class SegmentedReadoutRenderTest(
    private val widthDp: Int,
    private val fontScale: Float,
    private val dark: Boolean,
    private val language: String,
) {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longReadingsKeepSizeAndRemainAccessible() {
        val value = mutableStateOf(if (language == "fi") "123456789012345,67" else "123456789012345.67")
        val compound = mutableStateOf(false)
        var expectedHeight = 0f
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList(language))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(fontScale)) {
                    FonecheckTheme(darkTheme = dark) {
                        expectedHeight =
                            with(LocalDensity.current) {
                                FonecheckTheme.type.readout.lineHeight
                                    .toPx()
                            }
                        Column(
                            Modifier
                                .width(widthDp.dp)
                                .testTag("overflow-review"),
                        ) {
                            ReadoutWindow {
                                if (compound.value) {
                                    WindowFigure("${value.value} GB", modifier = Modifier.testTag("reading"))
                                } else {
                                    WindowReading(value.value, "GB", modifier = Modifier.testTag("reading"))
                                }
                            }
                        }
                    }
                }
            }
        }

        fun figure() = composeRule.onNodeWithText(value.value, useUnmergedTree = true)

        val numberBounds = figure().fetchSemanticsNode().boundsInRoot
        assertEquals("Overflow must not reduce digit height", expectedHeight, numberBounds.height, 1f)
        val unitBounds =
            composeRule
                .onNodeWithText("GB", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
        assertTrue("Unit must move below the number", unitBounds.top >= numberBounds.bottom)
        val texts =
            composeRule
                .onNodeWithTag("reading")
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
                .map { text ->
                    text.text
                }
        assertEquals(listOf(value.value, "GB"), texts)
        composeRule.onAllNodesWithText(value.value).assertCountEquals(1)
        val range =
            figure()
                .fetchSemanticsNode()
                .config[SemanticsProperties.HorizontalScrollAxisRange]
        assertTrue("Full number must be reachable", range.maxValue() > 0f)
        figure().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        val offset = range.value()
        assertTrue("Manual scrolling must work", offset > 0f)
        composeRule.runOnIdle { value.value = value.value.replace('7', '8') }
        composeRule.waitForIdle()
        assertEquals("Live updates must preserve position", offset, range.value(), 1f)
        if (InstrumentationRegistry.getArguments().getString("readoutReview") == "true") {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getExternalFilesDir(null)?.let { externalFilesDir ->
                val output = File(externalFilesDir, "readout-overflow").apply { mkdirs() }
                val bitmap =
                    composeRule
                        .onNodeWithTag("overflow-review")
                        .captureToImage()
                        .asAndroidBitmap()
                File(output, "$widthDp-$fontScale-$dark-$language.png").outputStream().use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
                bitmap.recycle()
            }
        }
        composeRule.runOnIdle {
            value.value = if (language == "fi") "1,25" else "1.25"
            compound.value = true
        }
        val shortBounds = figure().fetchSemanticsNode().boundsInRoot
        assertEquals("Short and long readings keep the same size", expectedHeight, shortBounds.height, 1f)
        assertEquals(
            0f,
            figure()
                .fetchSemanticsNode()
                .config[SemanticsProperties.HorizontalScrollAxisRange]
                .maxValue(),
            1f,
        )
        val shortUnit =
            composeRule
                .onNodeWithText("GB", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
        if (fontScale <= 1.3f) {
            assertTrue("A fitting unit stays beside the value", shortUnit.left >= shortBounds.right)
        } else {
            assertTrue("Large font scale keeps the stacked layout", shortUnit.top >= shortBounds.bottom)
        }
        assertEquals(
            listOf(value.value, "GB"),
            composeRule
                .onNodeWithTag("reading")
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
                .map { text ->
                    text.text
                },
        )
    }

    @Test
    fun captureMatchedReadouts() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("readoutReview") == "true")
        val sample = mutableIntStateOf(0)
        val candidate = mutableStateOf(false)
        var density = 0f
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList(language))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(fontScale)) {
                    density = LocalDensity.current.density
                    FonecheckTheme(darkTheme = dark) {
                        ReadoutSpecimen(
                            sample = sample.intValue,
                            candidate = candidate.value,
                            widthDp = widthDp,
                            language = language,
                        )
                    }
                }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "readout-review").apply { mkdirs() }
        val samples = if (widthDp == 380) (0..28).toList() else (10..19).toList() + (21..26).toList()
        samples.forEach { index ->
            val bitmaps =
                listOf(false, true).map { alternative ->
                    composeRule.runOnIdle {
                        sample.intValue = index
                        candidate.value = alternative
                    }
                    composeRule.waitForIdle()
                    if (index == 14) {
                        // Query the actual shared components, without a specimen semantics override.
                        composeRule
                            .onAllNodesWithText(if (language == "fi") "1,25" else "1.25")
                            .assertCountEquals(1)
                        composeRule.onAllNodesWithText("GB").assertCountEquals(1)
                    }
                    composeRule
                        .onNodeWithTag("readout-specimen")
                        .captureToImage()
                        .asAndroidBitmap()
                        .also {
                            // Fail instead of presenting a narrower device capture as the requested viewport.
                            assertEquals((widthDp * density).roundToInt(), it.width)
                        }
                }
            val baseline = bitmaps[0]
            val proposed = bitmaps[1]
            // The approved overflow policy may stack at different widths for the two geometries.
            val pair =
                Bitmap.createBitmap(
                    baseline.width * 2,
                    maxOf(baseline.height, proposed.height),
                    Bitmap.Config.ARGB_8888,
                )
            Canvas(pair).apply {
                drawBitmap(baseline, 0f, 0f, null)
                drawBitmap(proposed, baseline.width.toFloat(), 0f, null)
            }
            val name = "$widthDp-$fontScale-${if (dark) "dark" else "light"}-$language-$index.png"
            File(output, name).outputStream().use {
                assertTrue(pair.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            pair.recycle()
            bitmaps.forEach(Bitmap::recycle)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}dp scale={1} dark={2} locale={3}")
        fun configurations(): List<Array<Any>> =
            listOf(380 to 1f, 320 to 1f, 320 to 1.3f, 320 to 2f).flatMap { (width, scale) ->
                listOf(false, true).flatMap { dark ->
                    listOf("en", "fi").map { language -> arrayOf<Any>(width, scale, dark, language) }
                }
            }
    }
}
