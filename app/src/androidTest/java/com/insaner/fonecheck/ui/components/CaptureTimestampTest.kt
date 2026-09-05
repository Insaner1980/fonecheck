package com.insaner.fonecheck.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CaptureTimestampTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureTimestampShowsTheLiveLabelAndUpdatedTimeInSeparateNodes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val capturedAt = Instant.parse("2026-08-17T14:05:00Z")

        composeRule.setContent {
            FonecheckTheme {
                CaptureTimestamp(
                    capturedAt = capturedAt,
                    modifier = Modifier.testTag("capture_timestamp"),
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.live_state_label)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.live_state_updated_at,
                    formatCaptureTimestamp(
                        capturedAt,
                        context.resources.configuration.locales[0],
                    ),
                ),
            ).assertIsDisplayed()
    }

    @Test
    fun captureTimestampKeepsItsOriginalOuterSpacing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val capturedAt = Instant.parse("2026-08-17T14:05:00Z")
        val label = context.getString(R.string.live_state_label)
        val value =
            context.getString(
                R.string.live_state_updated_at,
                formatCaptureTimestamp(
                    capturedAt,
                    context.resources.configuration.locales[0],
                ),
            )

        composeRule.setContent {
            FonecheckTheme {
                CaptureTimestamp(
                    capturedAt = capturedAt,
                    modifier = Modifier.testTag("capture_timestamp"),
                )
            }
        }

        val timestampBounds =
            composeRule.onNodeWithTag("capture_timestamp").fetchSemanticsNode().boundsInRoot
        val labelBounds = composeRule.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
        val valueBounds = composeRule.onNodeWithText(value).fetchSemanticsNode().boundsInRoot
        val density = context.resources.displayMetrics.density

        assertEquals(4f * density, labelBounds.top - timestampBounds.top, 0.5f)
        assertEquals(8f * density, timestampBounds.bottom - valueBounds.bottom, 0.5f)
    }
}
