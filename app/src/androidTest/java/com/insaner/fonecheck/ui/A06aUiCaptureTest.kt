package com.insaner.fonecheck.ui

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeLayoutState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** One selector, with a deterministic precompose/measure/place/scroll lifecycle in all 16 configurations. */
@RunWith(AndroidJUnit4::class)
class A06aUiCaptureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placementEligibilityKeepsVisibilityAndGeometryChecks() {
        var fixture by mutableStateOf(PlacementFixture(276, 1f, false, "en"))
        composeRule.setContent {
            key(fixture) {
                DeviceConfigurationOverride(
                    DeviceConfigurationOverride
                        .Locales(LocaleList(fixture.language))
                        .then(DeviceConfigurationOverride.FontScale(fixture.fontScale))
                        .then(DeviceConfigurationOverride.ForcedSize(DpSize(fixture.widthDp.dp, 160.dp))),
                ) {
                    fixture.resourceLanguage = LocalConfiguration.current.locales[0].language
                    fixture.textLanguage = LocalLocale.current.platformLocale.language
                    FonecheckTheme(darkTheme = fixture.dark) { PlacementContent(fixture) }
                }
            }
        }

        for ((width, scale) in listOf(276 to 1f, 320 to 1f, 276 to 2f, 320 to 2f)) {
            for (dark in listOf(false, true)) {
                for (language in listOf("en", "fi")) {
                    val current = PlacementFixture(width, scale, dark, language)
                    composeRule.runOnIdle { fixture = current }
                    val root = node("placement-viewport")
                    val density = root.layoutInfo.density
                    assertEquals(width.toFloat(), root.boundsInRoot.width / density.density, 1f / density.density)
                    assertEquals(160f, root.boundsInRoot.height / density.density, 1f / density.density)
                    assertEquals(scale, density.fontScale, 0.001f)
                    assertEquals(language, current.resourceLanguage)
                    assertEquals(language, current.textLanguage)
                    val capture =
                        A06aUiCapture(
                            composeRule,
                            A06aUiCell(
                                "${javaClass.name}#placementEligibilityKeepsVisibilityAndGeometryChecks",
                                "regression",
                                language,
                                if (dark) "dark" else "light",
                                "U0",
                                width.toFloat(),
                                "placement-regression",
                                false,
                            ),
                            "component",
                        )
                    val handle =
                        composeRule.runOnIdle {
                            current.layoutState.precompose("pending", current.pendingContent)
                        }
                    try {
                        assertEquals(1, handle.placeablesCount)
                        assertUnplaced(capture, current, "precomposed")
                        composeRule.runOnIdle {
                            handle.premeasure(
                                0,
                                Constraints.fixed(root.size.width, with(density) { 56.dp.roundToPx() }),
                            )
                        }
                        val pending = node("pending")
                        assertEquals(root.size.width, handle.getSize(0).width)
                        assertEquals(with(density) { 56.dp.roundToPx() }, handle.getSize(0).height)
                        assertTrue(pending.size.width > 0 && pending.size.height > 0)
                        // Prove this fixture exercises the old coordinate-only acceptance path.
                        val origin = pending.positionInRoot
                        val full =
                            Rect(origin.x, origin.y, origin.x + pending.size.width, origin.y + pending.size.height)
                        val region = root.boundsInRoot
                        assertTrue(full.left >= region.left - 1f && full.right <= region.right + 1f)
                        assertTrue(full.top >= region.top - 1f && full.bottom <= region.bottom + 1f)
                        val overlap = pending.touchBoundsInRoot.intersect(node("live").touchBoundsInRoot)
                        assertTrue(overlap.width > 1f && overlap.height > 1f)
                        assertUnplaced(capture, current, "premeasured")

                        composeRule.runOnIdle { current.placePending = true }
                        assertEquals(pending.id, node("pending").id)
                        assertPlaced(capture, current, "placed", fullyVisible = true)
                        scroll(with(density) { 80.dp.toPx() })
                        assertPlaced(capture, current, "clipped", fullyVisible = false)
                        val clipped = node("pending")
                        assertTrue(clipped.boundsInRoot.height > 0f)
                        assertTrue(clipped.boundsInRoot.height < clipped.size.height)
                        scroll(with(density) { 100.dp.toPx() })
                        val offscreen = node("pending")
                        assertTrue(offscreen.layoutInfo.isPlaced)
                        assertEquals(0f, offscreen.boundsInRoot.height, 1f)
                        val hidden = inspect(capture)
                        assertFalse(hasTag(hidden.getJSONArray("targets"), "pending"))
                        assertFalse(hasTag(hidden.getJSONArray("texts"), "pending_text"))
                        assertFalse(hasTag(hidden.getJSONArray("excluded"), "pending"))
                        assertTargets(capture)
                        log(current, "offscreen")
                        scroll(with(density) { (-180).dp.toPx() })
                        assertEquals(0, current.scrollState.value)
                        assertEquals(pending.id, node("pending").id)
                        assertPlaced(capture, current, "returned", fullyVisible = true)

                        composeRule.runOnIdle { current.control = Control.OVERLAP }
                        assertOverlapFailure(capture, node("live"), node("pending"))
                        log(current, "placed-overlap-rejected")
                        for (control in listOf(Control.SMALL_WIDTH, Control.SMALL_HEIGHT)) {
                            composeRule.runOnIdle { current.control = control }
                            val live = node("live")
                            val touch = live.touchBoundsInRoot
                            val dimension = if (control == Control.SMALL_WIDTH) "width" else "height"
                            assertTrue(
                                "$control layout=${live.size.width}x${live.size.height}px " +
                                    "touch=${touch.width}x${touch.height}px density=${density.density}",
                                (if (dimension == "width") touch.width else touch.height) + 1f < 48f * density.density,
                            )
                            val failure = assertThrows(AssertionError::class.java) { assertTargets(capture) }
                            assertTrue(failure.message.orEmpty().contains("$dimension below 48dp"))
                            log(current, "$dimension-below-48dp-rejected")
                        }
                        composeRule.runOnIdle { current.control = Control.NESTED_OVERLAP }
                        assertOverlapFailure(capture, node("live"), node("nested"))
                        log(current, "independent-clickable-ancestor-descendant-rejected")
                        composeRule.runOnIdle { current.control = Control.NORMAL }
                        assertPlaced(capture, current, "configuration-pass", fullyVisible = true)
                    } finally {
                        composeRule.runOnIdle { handle.dispose() }
                    }
                }
            }
        }
        Log.i("A06aUiCaptureRegression", "COMPLETE 16 configurations")
    }

    private fun assertUnplaced(
        capture: A06aUiCapture,
        fixture: PlacementFixture,
        stage: String,
    ) {
        val pending = node("pending")
        assertTrue(pending.layoutInfo.isAttached)
        assertTrue(pending.layoutInfo.coordinates.isAttached)
        assertFalse(pending.layoutInfo.isPlaced)
        val observed = inspect(capture)
        assertFalse(hasTag(observed.getJSONArray("targets"), "pending"))
        assertFalse(hasTag(observed.getJSONArray("texts"), "pending_text"))
        for (tag in listOf("pending", "pending_text")) {
            val excluded = entry(observed.getJSONArray("excluded"), tag)
            assertEquals(node(tag).id, excluded.getInt("semanticsId"))
            assertTrue(excluded.getBoolean("isAttached"))
            assertFalse(excluded.getBoolean("isPlaced"))
            assertFalse(excluded.getBoolean("geometryEligible"))
            assertTrue(excluded.has("parentSemanticsId") && excluded.has("parentLayoutSemanticsId"))
            assertTrue(excluded.has("bounds") && excluded.has("unclippedOriginPx") && excluded.has("target"))
            assertFalse(excluded.has("geometryMeasurementGap"))
            log(fixture, stage, excluded)
        }
        assertTargets(capture)
    }

    private fun assertPlaced(
        capture: A06aUiCapture,
        fixture: PlacementFixture,
        stage: String,
        fullyVisible: Boolean,
    ) {
        val observed = inspect(capture)
        val target = entry(observed.getJSONArray("targets"), "pending")
        assertTrue(target.getBoolean("geometryEligible") && target.getBoolean("isPlaced"))
        assertTrue(target.getBoolean("visibleInViewport"))
        assertEquals(fullyVisible, target.getBoolean("fullyVisibleInViewport"))
        assertFalse(hasTag(observed.getJSONArray("excluded"), "pending"))
        if (fullyVisible) {
            val text = entry(observed.getJSONArray("texts"), "pending_text")
            assertTrue(text.getBoolean("visibleInViewport"))
            assertTrue(text.getJSONArray("visibleLines").length() > 0)
            assertEquals(fixture.privacyLabel, text.getString("text"))
            assertEquals(fixture.textColor, text.getString("textColor"))
        }
        assertTargets(capture)
        log(fixture, stage, target)
    }

    private fun inspect(capture: A06aUiCapture): JSONObject {
        val root = node("placement-viewport")
        val texts = JSONArray()
        val targets = JSONArray()
        val excluded = JSONArray()
        composeRule.runOnIdle { capture.inspectTree(root, root.boundsInRoot, texts, targets, true, excluded) }
        return JSONObject().put("texts", texts).put("targets", targets).put("excluded", excluded)
    }

    private fun assertTargets(capture: A06aUiCapture) {
        val root = node("placement-viewport")
        composeRule.runOnIdle { capture.assertVisibleTargets(root, root.boundsInRoot) }
    }

    private fun assertOverlapFailure(
        capture: A06aUiCapture,
        first: SemanticsNode,
        second: SemanticsNode,
    ) {
        assertTrue(first.layoutInfo.isPlaced && second.layoutInfo.isPlaced)
        val failure = assertThrows(AssertionError::class.java) { assertTargets(capture) }
        val message = failure.message.orEmpty()
        assertTrue(message.contains("Interactive targets overlap"))
        assertTrue(message.contains("id=${first.id},") && message.contains("id=${second.id},"))
    }

    private fun scroll(pixels: Float) {
        composeRule.onNodeWithTag("placement-viewport").performSemanticsAction(SemanticsActions.ScrollBy) {
            assertTrue(it(0f, pixels))
        }
        composeRule.waitForIdle()
    }

    private fun node(tag: String): SemanticsNode =
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()

    private fun hasTag(
        array: JSONArray,
        tag: String,
    ): Boolean = (0 until array.length()).any { array.getJSONObject(it).optString("tag") == tag }

    private fun entry(
        array: JSONArray,
        tag: String,
    ): JSONObject = (0 until array.length()).map(array::getJSONObject).single { it.optString("tag") == tag }

    private fun log(
        fixture: PlacementFixture,
        stage: String,
        evidence: JSONObject? = null,
    ) {
        Log.i(
            "A06aUiCaptureRegression",
            JSONObject()
                .put("widthDp", fixture.widthDp)
                .put("fontScale", fixture.fontScale)
                .put("language", fixture.language)
                .put("dark", fixture.dark)
                .put("stage", stage)
                .put("evidence", evidence ?: JSONObject.NULL)
                .toString(),
        )
    }

    @Composable
    private fun PlacementContent(fixture: PlacementFixture) {
        fixture.privacyLabel = stringResource(R.string.settings_privacy)
        fixture.textColor = FonecheckTheme.colors.textPrimary.toString()
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(FonecheckTheme.colors.panel)
                .clipToBounds()
                .verticalScroll(fixture.scrollState)
                .testTag("placement-viewport"),
        ) {
            SubcomposeLayout(fixture.layoutState, Modifier.fillMaxWidth().height(360.dp)) { constraints ->
                val liveWidth = if (fixture.control == Control.SMALL_WIDTH) 24.dp.roundToPx() else constraints.maxWidth
                val liveHeight = if (fixture.control == Control.SMALL_HEIGHT) 24.dp.roundToPx() else 56.dp.roundToPx()
                val live =
                    subcompose("live") {
                        val viewConfiguration = LocalViewConfiguration.current
                        val liveViewConfiguration =
                            if (fixture.control == Control.SMALL_WIDTH || fixture.control == Control.SMALL_HEIGHT) {
                                object : ViewConfiguration by viewConfiguration {
                                    override val minimumTouchTargetSize = DpSize.Zero
                                }
                            } else {
                                viewConfiguration
                            }
                        CompositionLocalProvider(LocalViewConfiguration provides liveViewConfiguration) {
                            ProbeTarget(
                                "live",
                                if (fixture.control == Control.NORMAL || fixture.control == Control.OVERLAP) {
                                    R.string.settings_support
                                } else {
                                    null
                                },
                                nested = fixture.control == Control.NESTED_OVERLAP,
                            )
                        }
                    }.single().measure(Constraints.fixed(liveWidth, liveHeight))
                val pending =
                    if (fixture.placePending) {
                        subcompose("pending", fixture.pendingContent)
                            .single()
                            .measure(Constraints.fixed(constraints.maxWidth, 56.dp.roundToPx()))
                    } else {
                        null
                    }
                layout(constraints.maxWidth, 360.dp.roundToPx()) {
                    live.placeRelative(0, 0)
                    pending?.placeRelative(0, if (fixture.control == Control.OVERLAP) 0 else 72.dp.roundToPx())
                }
            }
        }
    }

    @Composable
    private fun ProbeTarget(
        tag: String,
        labelRes: Int?,
        nested: Boolean = false,
    ) {
        // OnClick semantics use the local configuration's minimum touch target size.
        Box(
            Modifier.fillMaxSize().testTag(tag).semantics { onClick { true } },
            contentAlignment = Alignment.Center,
        ) {
            if (labelRes != null) {
                BasicText(
                    AnnotatedString(stringResource(labelRes)),
                    modifier = Modifier.testTag("${tag}_text"),
                    style = FonecheckTheme.type.rowLabel.copy(color = FonecheckTheme.colors.textPrimary),
                )
            }
            if (nested) ProbeTarget("nested", null)
        }
    }

    private enum class Control { NORMAL, OVERLAP, SMALL_WIDTH, SMALL_HEIGHT, NESTED_OVERLAP }

    private inner class PlacementFixture(
        val widthDp: Int,
        val fontScale: Float,
        val dark: Boolean,
        val language: String,
    ) {
        // Both paths must retain the same composition content and its semantics nodes.
        val pendingContent: @Composable () -> Unit = { ProbeTarget("pending", R.string.settings_privacy) }
        val layoutState = SubcomposeLayoutState()
        val scrollState = ScrollState(0)
        var placePending by mutableStateOf(false)
        var control by mutableStateOf(Control.NORMAL)
        var resourceLanguage = ""
        var textLanguage = ""
        var privacyLabel = ""
        var textColor = ""
    }
}
