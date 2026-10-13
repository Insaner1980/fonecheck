package com.insaner.fonecheck.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import android.system.Os
import android.system.OsConstants
import android.system.StructPollfd
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.theme.DarkFonecheckColors
import com.insaner.fonecheck.ui.theme.LightFonecheckColors
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil

internal const val A06A_FIXTURE_ROOT = "a06a_fixture_panel"

/** One explicitly selected cell. W0 must come from the future native baseline measurement. */
internal data class A06aUiCell(
    val selector: String,
    val state: String,
    val locale: String,
    val theme: String,
    val profile: String,
    val normalContentDp: Float,
    val runId: String,
    val capture: Boolean,
) {
    val dark get() = theme == "dark"
    val fontScale get() = if (profile == "U2" || profile == "U3") 2f else 1f
    val contentDp get() = if (profile == "U1" || profile == "U3") 320f else normalContentDp

    fun localizedContext(): Context {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        return base.createConfigurationContext(
            Configuration(base.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(this@A06aUiCell.locale))
            },
        )
    }
}

/** Outer rule: an ordinary suite/discovery cannot launch these activities or a default matrix. */
internal class A06aUiSelection : TestRule {
    lateinit var cell: A06aUiCell
        private set

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                val args = InstrumentationRegistry.getArguments()
                val required =
                    listOf(
                        "class",
                        "a06aState",
                        "a06aLocale",
                        "a06aTheme",
                        "a06aProfile",
                        "a06aNormalContentDp",
                        "a06aRunId",
                    )
                assumeTrue("A06a requires one explicit selector and cell", required.all { args.containsKey(it) })
                val selector = "${description.className}#${description.methodName}"
                require(args.getString("class") == selector) { "Select exactly $selector" }
                val states =
                    when (description.methodName) {
                        "settingsState" -> (1..8).map { "S$it" }
                        "languageState" -> listOf("L1", "L2", "L3")
                        "licensesContent" -> listOf("N1")
                        "cameraFullAccessContent" -> listOf("F1")
                        "reachableScreen" -> listOf("A1", "A2", "A3")
                        "systemThemeSmoke" -> listOf("A1")
                        else -> error("Unsupported A06a method")
                    }
                val state = args.getString("a06aState").orEmpty()
                val locale = args.getString("a06aLocale").orEmpty()
                val theme = args.getString("a06aTheme").orEmpty()
                val profile = args.getString("a06aProfile").orEmpty()
                val normal = args.getString("a06aNormalContentDp")?.toFloatOrNull()
                val runId = args.getString("a06aRunId").orEmpty()
                val capture = args.getString("a06aUiCapture") ?: "false"
                require(state in states && locale in listOf("en", "fi") && theme in listOf("light", "dark"))
                require(profile in listOf("U0", "U1", "U2", "U3"))
                require(normal != null && normal.isFinite() && normal > 0f) { "Measured W0 is required" }
                require(runId.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,79}"))) { "Invalid fresh run ID" }
                require(capture == "true" || capture == "false")
                require(description.methodName != "systemThemeSmoke" || profile == "U0")
                require(Build.VERSION.SDK_INT >= 30) { "BLOCKED: window measurements require API 30 or newer" }
                cell = A06aUiCell(selector, state, locale, theme, profile, normal, runId, capture == "true")
                base.evaluate()
            }
        }
}

/** Measurements and opt-in evidence only; this helper owns no production state or host configuration. */
internal class A06aUiCapture(
    private val rule: ComposeTestRule,
    val cell: A06aUiCell,
    private val kind: String,
    val inputs: JSONObject = JSONObject(),
) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val viewport get() = rule.onNode(hasScrollToIndexAction(), useUnmergedTree = true)
    private var measuredBudgetPx = 0f
    private var sequence = 0
    private val directory: String by lazy {
        check(instrumentation.context.packageName == "com.insaner.fonecheck.test")
        require(cell.runId.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,79}"))) { "Invalid fresh run ID" }
        val parent = "cache/a06a-ui-matrix"
        val path = "$parent/${cell.runId}"
        runAsTestPackage(
            "test -d cache && test ! -L cache && test ! -L $parent && mkdir -p $parent && mkdir $path",
        )
        path
    }
    private val apkHashes: JSONObject by lazy {
        JSONObject()
            .put("appApkSha256", sha256(File(instrumentation.targetContext.applicationInfo.sourceDir)))
            .put("testApkSha256", sha256(File(instrumentation.context.applicationInfo.sourceDir)))
            .put("newBuildFingerprint", JSONObject.NULL)
            .put("provenanceRequirement", "Bind to a new owner build input manifest, signer and runner receipt")
    }

    fun guard(block: () -> Unit) {
        try {
            block()
        } catch (failure: Throwable) {
            try {
                val dialog = rule.onAllNodes(isDialog()).fetchSemanticsNodes().isNotEmpty()
                checkpoint("failure", dialog = dialog, verify = false, failure = failure)
            } catch (captureFailure: Throwable) {
                if (captureFailure !== failure) failure.addSuppressed(captureFailure)
            }
            throw failure
        }
    }

    fun bring(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        viewport.performScrollToNode(matcher)
        return rule.onNode(matcher).performScrollTo().assertIsDisplayed()
    }

    fun admit(
        anchor: SemanticsMatcher,
        textWidth: Boolean = false,
    ) {
        val interaction = bring(anchor)
        val node = interaction.fetchSemanticsNode()
        val density = node.layoutInfo.density
        assertEquals(cell.fontScale, density.fontScale, 0.001f)
        val view = view(node)
        val locale =
            if (kind == "component") {
                inputs.getString("composeResourceLocales").substringBefore(',')
            } else {
                view.resources.configuration.locales[0]
                    .language
            }
        assertEquals(cell.locale, locale)
        val width =
            if (textWidth) {
                textLayout(node)
                    .layoutInput.constraints.maxWidth
                    .toFloat()
            } else {
                node.size.width.toFloat()
            }
        assertEquals("Effective full content width", cell.contentDp, width / density.density, 1f / density.density)
        assertEquals(viewport.fetchSemanticsNode().boundsInRoot.width, width, 1f)
        panelBounds()
        viewport.performScrollToIndex(0)
        checkpoint("top")
    }

    fun assertTheme(dark: Boolean) {
        val label = cell.localizedContext().getString(R.string.settings_theme)
        val text = bring(hasText(label)).fetchSemanticsNode()
        val actual = textLayout(text).layoutInput.style.color
        assertEquals(if (dark) DarkFonecheckColors.textMuted else LightFonecheckColors.textMuted, actual)
        inputs.put("themeObserved", if (dark) "dark" else "light")
    }

    fun observeRow(
        label: String,
        expectedValue: String? = null,
    ) {
        val labelNode =
            rule
                .onNode(hasText(label), useUnmergedTree = true)
                .fetchSemanticsNode()
        assertTrue("Row label must be attached and placed: ${identity(labelNode)}", geometryEligible(labelNode))
        val width = viewport.fetchSemanticsNode().boundsInRoot.width
        val rowInfo =
            generateSequence(labelNode.layoutInfo.parentInfo) { it.parentInfo }
                .first {
                    it.isAttached && it.isPlaced && it.coordinates.isAttached &&
                        abs(it.coordinates.size.width - width) <= 1f
                }
        val rowOrigin = rowInfo.coordinates.localToRoot(Offset.Zero)
        val row = Rect(rowOrigin.x, rowOrigin.y, rowOrigin.x + rowInfo.width, rowOrigin.y + rowInfo.height)
        val values =
            rule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .filter(::geometryEligible)
                .filter {
                    val origin = it.positionInRoot
                    val full = Rect(origin.x, origin.y, origin.x + it.size.width, origin.y + it.size.height)
                    it.id != labelNode.id && contained(full, row)
                }
        check(values.size == 1) { "Ambiguous row value for $label: ${values.size}" }
        val value = values.single()
        measuredBudgetPx += value.size.height
        val visible = viewport.fetchSemanticsNode().boundsInRoot
        if (row.height <= visible.height && row.bottom > visible.bottom) scrollBy(row.bottom - visible.bottom)
        val layout = textLayout(value)
        val text =
            layout.layoutInput.text.text
                .replace("\u200B", "")
        expectedValue?.let { assertEquals(it, text) }
        val a = labelNode.boundsInRoot
        val b = value.boundsInRoot
        assertTrue("Label and value overlap: $label", a.right <= b.left + 1f || a.bottom <= b.top + 1f)
        inputs.put(
            "row:$label",
            JSONObject()
                .put("value", text)
                .put("row", rect(rowInfo.coordinates.boundsInRoot(), value.layoutInfo.density.density)),
        )
    }

    fun languageRows(
        order: List<AppLanguage>,
        selectedLanguage: AppLanguage,
    ) {
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup)).assertCountEquals(1)
        var previousBottom = Float.NEGATIVE_INFINITY
        order.forEach { language ->
            val interaction =
                visit(hasTestTag(languageTag(language)), "language_${language.name.lowercase()}", action = true)
            interaction.assert(hasText(cell.localizedContext().getString(language.labelResId)))
            val node = interaction.fetchSemanticsNode()
            val origin = node.positionInRoot.y
            // A single lazy item owns all rows. Its scroll offset is applied equally to every row.
            val systemY =
                rule
                    .onNode(hasTestTag(languageTag(AppLanguage.SYSTEM)))
                    .fetchSemanticsNode()
                    .positionInRoot.y
            val relativeTop = origin - systemY
            assertTrue("UI collator row order", relativeTop >= previousBottom - 1f)
            previousBottom = relativeTop + node.size.height
            interaction.assert(selected(language == selectedLanguage))
        }
        a06aAssertOneSelection(rule, selectedLanguage)
    }

    fun visit(
        matcher: SemanticsMatcher,
        checkpoint: String?,
        action: Boolean = false,
    ): SemanticsNodeInteraction {
        val interaction = bring(matcher)
        val node = interaction.fetchSemanticsNode()
        measuredBudgetPx += node.size.height + 24f * node.layoutInfo.density.density
        if (action) assertTarget(node)
        if (checkpoint != null) this.checkpoint(checkpoint)
        return interaction
    }

    /** Capture with 25% overlap. The bound comes from measured probes, not a guessed scroll length. */
    fun sweep() {
        viewport.performScrollToIndex(0)
        val height = viewport.fetchSemanticsNode().boundsInRoot.height
        assertTrue("Usable scroll viewport", height > 0f)
        val step = height * 0.75f
        val limit = ceil((measuredBudgetPx + 2f * height) / step).toInt() + 4
        var moved = false
        var measuredOffsetPx = 0f
        for (index in 0..limit) {
            checkpoint("viewport_$index")
            val before = scrollPosition()
            val beforePositions = visibleTextPositions()
            scrollBy(step)
            val after = scrollPosition()
            val afterPositions = visibleTextPositions()
            val deltas =
                beforePositions.keys.intersect(afterPositions.keys).map {
                    beforePositions.getValue(it) - afterPositions.getValue(it)
                }
            val delta = checkNotNull(deltas.maxOrNull()) { "BLOCKED: no overlapping scroll landmark" }
            if (abs(delta) < 0.01f && abs(after.first - before.first) < 0.0001f) {
                assertEquals("Scroll stopped before its end", after.second, after.first, 0.0001f)
                inputs.put("scrollChanged", moved).put("bottomReached", true)
                checkpoint("bottom")
                return
            }
            assertTrue("Measured scroll displacement must advance", delta > 0f)
            measuredOffsetPx += delta
            inputs.put("measuredScrollOffsetPx", measuredOffsetPx)
            moved = true
        }
        error("Scroll exceeded its measured bound; bottom NOT PROVEN")
    }

    fun licensesEnd(fullText: String) {
        val matcher = hasText(fullText)
        val node = rule.onNode(matcher, useUnmergedTree = true).fetchSemanticsNode()
        val layout = textLayout(node)
        val finalText = "OTHER DEALINGS IN THE FONT SOFTWARE."
        assertTrue(fullText.trimEnd().endsWith(finalText))
        val lastCharacter = fullText.indexOfLast { !it.isWhitespace() }
        val firstCharacter = fullText.lastIndexOf(finalText)
        val firstLine = layout.getLineForOffset(firstCharacter)
        val lastLine = layout.getLineForOffset(lastCharacter)
        val origin = node.positionInRoot
        val visible = viewport.fetchSemanticsNode().boundsInRoot
        assertTrue("Final notice line top visible", origin.y + layout.getLineTop(firstLine) >= visible.top - 1f)
        assertTrue("Final notice line bottom visible", origin.y + layout.getLineBottom(lastLine) <= visible.bottom + 1f)
        assertTrue("Final notice has trailing padding", origin.y + node.size.height < visible.bottom)
        assertEquals(fullText.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = false))
        val selection = layout.getPathForRange(firstCharacter, lastCharacter + 1).getBounds()
        inputs.put(
            "finalNoticeSelectionGeometryInRoot",
            rect(
                Rect(
                    selection.left + origin.x,
                    selection.top + origin.y,
                    selection.right + origin.x,
                    selection.bottom + origin.y,
                ),
                node.layoutInfo.density.density,
            ),
        )
        checkpoint("final_notice_line")
    }

    fun assertTarget(node: SemanticsNode) {
        val target = clickParent(node)
        assertTrue("Target must be attached and placed: ${identity(target)}", geometryEligible(target))
        val bounds = target.touchBoundsInRoot
        val density = target.layoutInfo.density.density
        assertTrue("Target width is below 48dp: $bounds", bounds.width + 1f >= 48f * density)
        assertTrue("Target height is below 48dp: $bounds", bounds.height + 1f >= 48f * density)
        val region = if (kind == "fixture-dialog") rootBounds(target) else viewport.fetchSemanticsNode().boundsInRoot
        assertContained("Interactive target", bounds, region)
    }

    fun shellChrome(
        title: String,
        dark: Boolean = cell.dark,
    ) {
        val back =
            rule.onNode(
                hasContentDescription(
                    cell.localizedContext().getString(R.string.navigation_back),
                ),
            )
        val backNode = clickParent(back.assertIsDisplayed().fetchSemanticsNode())
        assertTrue("Back must be attached and placed: ${identity(backNode)}", geometryEligible(backNode))
        val titleNode =
            rule
                .onNode(hasText(title), useUnmergedTree = true)
                .assertIsDisplayed()
                .fetchSemanticsNode()
        val density = backNode.layoutInfo.density.density
        assertEquals(
            if (dark) DarkFonecheckColors.textPrimary else LightFonecheckColors.textPrimary,
            textLayout(titleNode).layoutInput.style.color,
        )
        inputs.put("themeObserved", if (dark) "dark" else "light")
        assertTrue(backNode.touchBoundsInRoot.width + 1f >= 48f * density)
        assertTrue(backNode.touchBoundsInRoot.height + 1f >= 48f * density)
        val content = viewport.fetchSemanticsNode().boundsInRoot
        assertTrue("Title overlaps content", titleNode.boundsInRoot.bottom <= content.top + 1f)
        assertTrue("Back overlaps content", backNode.touchBoundsInRoot.bottom <= content.top + 1f)
        assertTrue("Title overlaps Back", titleNode.boundsInRoot.left >= backNode.touchBoundsInRoot.right - 1f)
        assertContained("Back", backNode.touchBoundsInRoot, rootBounds(backNode))
        val insets = checkNotNull(ViewCompat.getRootWindowInsets(view(backNode).rootView))
        val safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val onScreen = backNode.positionOnScreen
        assertTrue("Back under status/cutout inset", onScreen.y >= safe.top - 1f)
        val activity = activity(view(backNode).context)
        val window = activity.windowManager.currentWindowMetrics.bounds
        assertTrue("Content under bottom inset", content.bottom <= window.height() - safe.bottom + 1f)
        assertTrue(insets.isVisible(WindowInsetsCompat.Type.statusBars()))
        assertTrue(insets.isVisible(WindowInsetsCompat.Type.navigationBars()))
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        assertFalse(controller.isAppearanceLightStatusBars)
        assertFalse(controller.isAppearanceLightNavigationBars)
        inputs.put("actualTitle", title).put("backTargetDp", rect(backNode.touchBoundsInRoot, density))
        checkpoint("shell_chrome")
    }

    /** Dialog root and Android window are deliberately separate from the background fixture. */
    fun dialogCheckpoint(name: String) {
        val root = rule.onNode(isDialog()).fetchSemanticsNode()
        assertEquals(
            cell.locale,
            view(root)
                .resources.configuration.locales[0]
                .language,
        )
        assertEquals(cell.fontScale, root.layoutInfo.density.fontScale, 0.001f)
        val context = cell.localizedContext()
        listOf(R.string.settings_delete_all_title, R.string.settings_delete_all_message).forEach { id ->
            val node =
                rule
                    .onNode(hasText(context.getString(id)), useUnmergedTree = true)
                    .assertIsDisplayed()
                    .fetchSemanticsNode()
            assertContained(
                "Dialog text",
                Rect(
                    node.positionInRoot.x,
                    node.positionInRoot.y,
                    node.positionInRoot.x + node.size.width,
                    node.positionInRoot.y + node.size.height,
                ),
                rootBounds(root),
            )
            assertEquals(cell.fontScale, node.layoutInfo.density.fontScale, 0.001f)
        }
        val title =
            rule
                .onNode(
                    hasText(context.getString(R.string.settings_delete_all_title)),
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
        assertEquals(
            if (cell.dark) DarkFonecheckColors.textPrimary else LightFonecheckColors.textPrimary,
            textLayout(title).layoutInput.style.color,
        )
        val a = dialogAction(hasTestTag("settings_confirm_delete_all")).fetchSemanticsNode()
        val b =
            dialogAction(hasText(context.getString(R.string.settings_delete_all_cancel)))
                .fetchSemanticsNode()
        val first = clickParent(a).touchBoundsInRoot
        val second = clickParent(b).touchBoundsInRoot
        assertFalse("Dialog actions overlap", first.overlaps(second))
        // ForcedSize controls the background only. Admit the separately configured native host window.
        val activity = activity(view(viewport.fetchSemanticsNode()).context)
        val insets =
            checkNotNull(ViewCompat.getRootWindowInsets(activity.window.decorView))
                .getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val density = activity.resources.displayMetrics.density
        assertEquals(
            "Native dialog host font scale",
            cell.fontScale,
            activity.resources.configuration.fontScale,
            0.001f,
        )
        val width =
            activity.windowManager.currentWindowMetrics.bounds
                .width() / density
        val candidate = cell.contentDp + 44f + (insets.left + insets.right) / density
        assertEquals(
            "Native dialog host window candidate; actual shell must be measured separately",
            candidate,
            width,
            1f / density,
        )
        checkpoint(name, dialog = true)
    }

    fun dialogAction(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        val interaction = rule.onNode(matcher).assertIsDisplayed()
        val target = clickParent(interaction.fetchSemanticsNode())
        assertTrue("Dialog target must be attached and placed: ${identity(target)}", geometryEligible(target))
        val bounds = target.touchBoundsInRoot
        val density = target.layoutInfo.density.density
        assertTrue("Dialog target width below 48dp", bounds.width + 1f >= 48f * density)
        assertTrue("Dialog target height below 48dp", bounds.height + 1f >= 48f * density)
        assertContained("Dialog action", bounds, rootBounds(target))
        return interaction
    }

    fun checkpoint(
        name: String,
        dialog: Boolean = false,
        verify: Boolean = true,
        failure: Throwable? = null,
    ) {
        rule.waitForIdle()
        val contentNode = viewport.fetchSemanticsNode()
        val root = if (dialog) rule.onNode(isDialog()).fetchSemanticsNode() else mainRoot()
        val density = contentNode.layoutInfo.density.density
        val texts = JSONArray()
        val targets = JSONArray()
        val excludedNodes = JSONArray()
        var validationFailure: Throwable? = failure
        rule.runOnIdle {
            try {
                inspectTree(
                    root,
                    if (dialog) rootBounds(root) else contentNode.boundsInRoot,
                    texts,
                    targets,
                    verify,
                    excludedNodes,
                )
                if (verify) assertVisibleTargets(root, if (dialog) rootBounds(root) else contentNode.boundsInRoot)
            } catch (error: Throwable) {
                if (validationFailure == null) validationFailure = error else validationFailure?.addSuppressed(error)
            }
        }
        try {
            val measured =
                JSONObject()
                    .put("selector", cell.selector)
                    .put("state", cell.state)
                    .put(
                        "screen",
                        inputs.optString(
                            "renderedScreen",
                            when (cell.state) {
                                "L1", "L2", "L3", "A2" -> "LanguageSelection"
                                "N1", "A3" -> "Licenses"
                                "F1" -> "Camera FullAccess"
                                else -> "Settings"
                            },
                        ),
                    ).put("uiLocaleRequested", cell.locale)
                    .put("themeRequested", cell.theme)
                    .put("themeObserved", inputs.opt("themeObserved") ?: JSONObject.NULL)
                    .put("profile", cell.profile)
                    .put("checkpoint", name)
                    .put("evidenceKind", if (dialog) "fixture-dialog" else kind)
                    .put(
                        "coordinateOrigins",
                        JSONObject()
                            .put("rootTextsTargets", "capture root ${root.id}, root-relative px")
                            .put("panelViewportEffectiveContent", "background root ${mainRoot().id}, root-relative px")
                            .put("decor", "screen-relative px; Android resource density")
                            .put("windowMetrics", "activity window bounds in display px"),
                    ).put("normalContentDpFromHostMeasurement", cell.normalContentDp)
                    .put("requestedContentDp", cell.contentDp)
                    .put("effectiveContent", rect(contentNode.boundsInRoot, density))
                    .put("composeDensity", density)
                    .put("composeFontScale", contentNode.layoutInfo.density.fontScale)
                    .put("root", rect(rootBounds(root), root.layoutInfo.density.density))
                    .put("viewport", rect(contentNode.boundsInRoot, density))
                    .put(
                        "panel",
                        if (verify) {
                            rect(panelBounds(), density)
                        } else {
                            runCatching { rect(panelBounds(), density) }
                                .getOrElse { JSONObject().put("measurementGap", it.toString()) }
                        },
                    ).put("scrollAxisValue", scrollPosition().first)
                    .put("scrollAxisMax", scrollPosition().second)
                    .put(
                        "scrollAxisUnits",
                        "Lazy semantic estimate; measured displacement is recorded separately in px",
                    ).put("inputs", inputs)
                    .put("texts", texts)
                    .put("interactiveTargets", targets)
                    .put("excludedGeometryNodes", excludedNodes)
                    .put("window", windowMeasurement(root))
                    .put(
                        "automaticVerdict",
                        if (validationFailure == null && verify) "CHECKPOINT_PASS" else "FAIL_OR_UNCHECKED",
                    ).put("failure", validationFailure?.toString() ?: JSONObject.NULL)
                    .put("visualVerdict", "NOT_REVIEWED")
                    .put("preservationRestorationVerdict", "REQUIRES_INDEPENDENT_HOST_DEVICE_GATES")
                    .put("newBuildProvenance", apkHashes)
            if (dialog) {
                measured
                    .put("dialogDensity", root.layoutInfo.density.density)
                    .put("dialogFontScale", root.layoutInfo.density.fontScale)
                    .put("backgroundRoot", rect(rootBounds(contentNode), density))
            }
            Log.i("A06aUiMatrix", measured.toString())
            if (cell.capture) {
                val stem = "${cell.state}_${cell.locale}_${cell.theme}_${cell.profile}_${sequence++}_$name"
                val fixture = if (kind == "component" && !dialog) rule.onNode(hasTestTag(A06A_FIXTURE_ROOT)) else null
                val bitmap =
                    if (fixture != null) {
                        fixture.captureToImage().asAndroidBitmap()
                    } else {
                        checkNotNull(
                            instrumentation.uiAutomation.takeScreenshot(),
                        ) { "Platform screenshot unavailable" }
                    }
                try {
                    if (fixture != null) {
                        assertEquals(fixture.fetchSemanticsNode().size.width, bitmap.width)
                    }
                    val png =
                        ByteArrayOutputStream().use {
                            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) { "PNG compression failed" }
                            it.toByteArray()
                        }
                    writeCapture("$stem.png", png)
                    writeCapture("$stem.json", (measured.toString(2) + "\n").toByteArray(Charsets.UTF_8))
                } finally {
                    bitmap.recycle()
                }
            }
        } catch (captureFailure: Throwable) {
            val primary = validationFailure ?: throw captureFailure
            if (captureFailure !== primary) primary.addSuppressed(captureFailure)
        }
        if (verify) validationFailure?.let { throw it }
    }

    private fun writeCapture(
        name: String,
        bytes: ByteArray,
    ) {
        require(name.matches(Regex("[A-Za-z0-9_-]+\\.(png|json)")))
        val path = "$directory/$name"
        // Keep each command below argument/Binder limits; only the first chunk creates the file.
        for (offset in bytes.indices step 24576) {
            val encoded = Base64.encodeToString(bytes, offset, minOf(24576, bytes.size - offset), Base64.NO_WRAP)
            val redirect = if (offset == 0) ">" else ">>"
            runAsTestPackage("printf $encoded | base64 -d $redirect$path")
        }
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        runAsTestPackage(
            "test -f $path && test ! -L $path && test \$(wc -c <$path) -eq ${bytes.size} && " +
                "test \$(sha256sum $path | cut -c 1-64) = $hash",
        )
    }

    private fun runAsTestPackage(script: String) {
        // UiAutomation tokenizes without shell quoting; decode one token before parsing redirects.
        val encoded =
            Base64.encodeToString(
                ("set -eu;set -C;$script && printf A06A_OK").toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP,
            )
        val argument = "eval\${IFS}\"\$(printf\${IFS}$encoded|base64\${IFS}-d)\""
        check(argument.none { it.isWhitespace() })
        val descriptor =
            instrumentation.uiAutomation.executeShellCommand(
                "/system/bin/timeout 15 run-as com.insaner.fonecheck.test /system/bin/sh -c $argument",
            )
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
            val poll =
                StructPollfd().apply {
                    fd = descriptor.fileDescriptor
                    events = OsConstants.POLLIN.toShort()
                }
            val deadline = SystemClock.elapsedRealtime() + 20000L
            ByteArrayOutputStream().use { receipt ->
                val buffer = ByteArray(1024)
                while (true) {
                    val remaining = (deadline - SystemClock.elapsedRealtime()).toInt()
                    check(remaining > 0 && Os.poll(arrayOf(poll), remaining) > 0) { "Test-cache command timed out" }
                    val count = input.read(buffer)
                    if (count < 0) break
                    receipt.write(buffer, 0, count)
                }
                check(receipt.toString("UTF-8") == "A06A_OK") { "Test-cache command did not confirm success" }
            }
        }
    }

    internal fun inspectTree(
        node: SemanticsNode,
        viewport: Rect,
        texts: JSONArray,
        targets: JSONArray,
        verify: Boolean,
        excludedNodes: JSONArray,
    ) {
        if (!geometryEligible(node)) {
            excludedNodes.put(
                geometryEvidence(node)
                    .put(
                        "text",
                        node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } ?: JSONObject.NULL,
                    ).put("visibleInViewport", false)
                    .put("fullyVisibleInViewport", false)
                    .put("clickable", node.config.contains(SemanticsActions.OnClick)),
            )
            node.children.forEach { inspectTree(it, viewport, texts, targets, verify, excludedNodes) }
            return
        }
        val bounds = node.boundsInRoot
        if (bounds.width > 0f && bounds.height > 0f) {
            val visible = bounds.intersect(viewport)
            if (node.config.contains(SemanticsActions.GetTextLayoutResult)) {
                val layout = textLayout(node)
                val visibleLines =
                    (0 until layout.lineCount).filter { line ->
                        visible.width > 0f && visible.height > 0f &&
                            node.positionInRoot.y + layout.getLineBottom(line) > visible.top &&
                            node.positionInRoot.y + layout.getLineTop(line) < visible.bottom
                    }
                val measurement =
                    geometryEvidence(node)
                        .put("text", layout.layoutInput.text.text)
                        .put("bounds", rect(bounds, node.layoutInfo.density.density))
                        .put("unclippedOriginPx", JSONArray(listOf(node.positionInRoot.x, node.positionInRoot.y)))
                        .put("layoutSizePx", JSONArray(listOf(layout.size.width, layout.size.height)))
                        .put("constraintWidthPx", layout.layoutInput.constraints.maxWidth)
                        .put(
                            "constraintsPx",
                            JSONObject()
                                .put("minWidth", layout.layoutInput.constraints.minWidth)
                                .put("maxWidth", layout.layoutInput.constraints.maxWidth)
                                .put("minHeight", layout.layoutInput.constraints.minHeight)
                                .put("maxHeight", layout.layoutInput.constraints.maxHeight),
                        ).put("didOverflowWidth", layout.didOverflowWidth)
                        .put("didOverflowHeight", layout.didOverflowHeight)
                        .put("hasVisualOverflow", layout.hasVisualOverflow)
                        .put(
                            "paragraphSizePx",
                            JSONArray(listOf(layout.multiParagraph.width, layout.multiParagraph.height)),
                        ).put("paragraphDidExceedMaxLines", layout.multiParagraph.didExceedMaxLines)
                        .put("maxLines", layout.layoutInput.maxLines)
                        .put("softWrap", layout.layoutInput.softWrap)
                        .put("overflow", layout.layoutInput.overflow.toString())
                        .put(
                            "lines",
                            JSONArray(
                                (0 until layout.lineCount).map { line ->
                                    JSONObject()
                                        .put("index", line)
                                        .put("leftPx", layout.getLineLeft(line))
                                        .put("rightPx", layout.getLineRight(line))
                                        .put("topPx", layout.getLineTop(line))
                                        .put("bottomPx", layout.getLineBottom(line))
                                        .put("baselinePx", layout.getLineBaseline(line))
                                        .put("characterStart", layout.getLineStart(line))
                                        .put("characterEnd", layout.getLineEnd(line))
                                        .put("visibleCharacterEnd", layout.getLineEnd(line, visibleEnd = true))
                                        .put("ellipsized", layout.isLineEllipsized(line))
                                },
                            ),
                        ).put("lineCount", layout.lineCount)
                        .put(
                            "textColor",
                            layout.layoutInput.style.color
                                .toString(),
                        ).put("visibleLines", JSONArray(visibleLines))
                        .put("visibleInViewport", visible.width > 0f && visible.height > 0f)
                        .put(
                            "visibleCharacterStart",
                            visibleLines.firstOrNull()?.let(layout::getLineStart) ?: JSONObject.NULL,
                        ).put(
                            "visibleCharacterEnd",
                            visibleLines.lastOrNull()?.let { layout.getLineEnd(it) } ?: JSONObject.NULL,
                        )
                texts.put(measurement)
                if (verify) {
                    assertFalse("Text layout overflow: ${layout.layoutInput.text}", layout.hasVisualOverflow)
                    for (line in 0 until layout.lineCount) {
                        assertFalse("Ellipsized text", layout.isLineEllipsized(line))
                        assertTrue("Horizontal text overflow", layout.getLineLeft(line) >= -1f)
                        assertTrue("Horizontal text overflow", layout.getLineRight(line) <= layout.size.width + 1f)
                    }
                    assertEquals(layout.layoutInput.text.length, layout.getLineEnd(layout.lineCount - 1))
                    val origin = node.positionInRoot
                    val horizontalRegion =
                        if (bounds.bottom > viewport.top && bounds.top < viewport.bottom) viewport else rootBounds(node)
                    assertTrue(
                        "Text beyond its region horizontally",
                        origin.x >= horizontalRegion.left - 1f &&
                            origin.x + layout.size.width <= horizontalRegion.right + 1f,
                    )
                }
            }
            if (node.config.contains(SemanticsActions.OnClick)) {
                targets.put(
                    geometryEvidence(node)
                        .put("target", rect(node.touchBoundsInRoot, node.layoutInfo.density.density))
                        .put("visibleInViewport", visible.width > 0f && visible.height > 0f)
                        .put("fullyVisibleInViewport", fullyVisible(node, viewport))
                        .put("enabled", !node.config.contains(SemanticsProperties.Disabled))
                        .put("selected", node.config.getOrElse(SemanticsProperties.Selected) { false })
                        .put("toggleState", node.config.getOrNull(SemanticsProperties.ToggleableState)?.name)
                        .put("tag", node.config.getOrElse(SemanticsProperties.TestTag) { "" }),
                )
            }
        }
        node.children.forEach { inspectTree(it, viewport, texts, targets, verify, excludedNodes) }
    }

    internal fun assertVisibleTargets(
        root: SemanticsNode,
        region: Rect,
    ) {
        val targets = mutableListOf<Pair<SemanticsNode, Rect>>()

        fun collect(node: SemanticsNode) {
            if (node.config.contains(SemanticsActions.OnClick) && fullyVisible(node, region)) {
                val bounds = node.touchBoundsInRoot
                val density = node.layoutInfo.density.density
                assertTrue(
                    "Visible interactive target width below 48dp: ${identity(node)} / $bounds",
                    bounds.width + 1f >= 48f * density,
                )
                assertTrue(
                    "Visible interactive target height below 48dp: ${identity(node)} / $bounds",
                    bounds.height + 1f >= 48f * density,
                )
                targets.forEach { (otherNode, other) ->
                    val overlap = bounds.intersect(other)
                    assertFalse(
                        "Interactive targets overlap: ${identity(node)} / $bounds and ${identity(otherNode)} / $other",
                        overlap.width > 1f && overlap.height > 1f,
                    )
                }
                targets.add(node to bounds)
            }
            node.children.forEach(::collect)
        }
        collect(root)
    }

    // Check layout ancestors too, as Compose 1.12.1's Android isDisplayed implementation does.
    private fun geometryEligible(node: SemanticsNode): Boolean =
        generateSequence(node.layoutInfo) { it.parentInfo }
            .all { it.isAttached && it.isPlaced && it.coordinates.isAttached }

    private fun fullyVisible(
        node: SemanticsNode,
        region: Rect,
    ): Boolean {
        if (!geometryEligible(node)) return false
        val visible = node.boundsInRoot.intersect(region)
        val origin = node.positionInRoot
        val full = Rect(origin.x, origin.y, origin.x + node.size.width, origin.y + node.size.height)
        return visible.width > 0f && visible.height > 0f && contained(full, region)
    }

    private fun identity(node: SemanticsNode): String =
        "id=${node.id}, tag=${node.config.getOrElse(SemanticsProperties.TestTag) { "" }}, " +
            "parent=${node.parent?.id}, layoutParent=${node.layoutInfo.parentInfo?.semanticsId}"

    private fun geometryEvidence(node: SemanticsNode): JSONObject {
        val info = node.layoutInfo
        val parent = info.parentInfo
        val coordinatesAttached = info.coordinates.isAttached
        val eligible = geometryEligible(node)
        val evidence =
            JSONObject()
                .put("semanticsId", node.id)
                .put("layoutSemanticsId", info.semanticsId)
                .put("parentSemanticsId", node.parent?.id ?: JSONObject.NULL)
                .put("parentLayoutSemanticsId", parent?.semanticsId ?: JSONObject.NULL)
                .put("tag", node.config.getOrElse(SemanticsProperties.TestTag) { "" })
                .put("isAttached", info.isAttached)
                .put("coordinatesAttached", coordinatesAttached)
                .put("isPlaced", info.isPlaced)
                .put("parentIsAttached", parent?.isAttached ?: JSONObject.NULL)
                .put("parentIsPlaced", parent?.isPlaced ?: JSONObject.NULL)
                .put("geometryEligible", eligible)
                .put("geometryMeaning", if (eligible) "PLACED" else "EXCLUDED_NOT_VISIBLE")
                .put("measuredLayoutSizePx", JSONArray(listOf(info.width, info.height)))
        if (info.isAttached && coordinatesAttached) {
            // Unplaced coordinates are diagnostic values only, never visibility or assertion evidence.
            runCatching {
                val origin = node.positionInRoot
                val density = info.density.density
                evidence
                    .put("bounds", rect(node.boundsInRoot, density))
                    .put("unclippedOriginPx", JSONArray(listOf(origin.x, origin.y)))
                    .put("unclippedSizePx", JSONArray(listOf(node.size.width, node.size.height)))
                    .put("target", rect(node.touchBoundsInRoot, density))
            }.onFailure { evidence.put("geometryMeasurementGap", it.toString()) }
        } else {
            evidence.put("geometryMeasurementGap", "Detached layout or coordinates; geometry not read")
        }
        return evidence
    }

    private fun panelBounds(): Rect {
        if (kind == "component" || kind == "fixture-dialog") {
            return rule.onNode(hasTestTag(A06A_FIXTURE_ROOT)).fetchSemanticsNode().boundsInRoot
        }
        val node = viewport.fetchSemanticsNode()
        val bounds = node.boundsInRoot
        val paddingPx = 32f * node.layoutInfo.density.density
        // Select an actually measured ancestor. The expected padding only validates its identity.
        val candidates =
            generateSequence(node.layoutInfo) { it.parentInfo }
                .filter { it.isAttached && it.isPlaced && it.coordinates.isAttached }
                .map { it.coordinates.boundsInRoot() }
                .filter {
                    abs(it.width - bounds.width - paddingPx) <= 1f &&
                        abs(it.top - bounds.top) <= 1f && abs(it.bottom - bounds.bottom) <= 1f
                }.distinct()
                .toList()
        check(candidates.size == 1) { "BLOCKED: actual shell panel bounds are ambiguous: $candidates" }
        return candidates.single()
    }

    private fun mainRoot(): SemanticsNode = generateSequence(viewport.fetchSemanticsNode()) { it.parent }.last()

    private fun rootBounds(node: SemanticsNode): Rect =
        generateSequence(node) { it.parent }.last().let {
            Rect(0f, 0f, it.size.width.toFloat(), it.size.height.toFloat())
        }

    private fun clickParent(node: SemanticsNode): SemanticsNode =
        generateSequence(node) { it.parent }.firstOrNull { it.config.contains(SemanticsActions.OnClick) }
            ?: error("No actual interactive parent")

    private fun textLayout(node: SemanticsNode): TextLayoutResult {
        assertTrue("Text must be attached and placed: ${identity(node)}", geometryEligible(node))
        val results = mutableListOf<TextLayoutResult>()
        val action = checkNotNull(node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action)
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action(results)
        } else {
            instrumentation.runOnMainSync { action(results) }
        }
        return results.single()
    }

    private fun scrollPosition(): Pair<Float, Float> {
        val axis = viewport.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
        return rule.runOnIdle { axis.value() to axis.maxValue() }
    }

    private fun visibleTextPositions(): Map<String, Float> {
        val root = viewport.fetchSemanticsNode()
        val positions = mutableMapOf<String, Float>()

        fun collect(node: SemanticsNode) {
            val visible = if (geometryEligible(node)) node.boundsInRoot.intersect(root.boundsInRoot) else Rect.Zero
            if (visible.width > 0f && visible.height > 0f &&
                node.config.contains(SemanticsActions.GetTextLayoutResult)
            ) {
                positions["${node.id}:${textLayout(node).layoutInput.text}"] = node.positionInRoot.y
            }
            node.children.forEach(::collect)
        }
        rule.runOnIdle { collect(root) }
        return positions
    }

    private fun scrollBy(pixels: Float) {
        viewport.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, pixels) }
        rule.waitForIdle()
    }

    private fun windowMeasurement(node: SemanticsNode): JSONObject {
        val view = view(node)
        var ancestor: android.view.ViewParent? = view.parent
        var dialogWindow: android.view.Window? = null
        while (ancestor != null) {
            if (ancestor is DialogWindowProvider) dialogWindow = ancestor.window
            ancestor = ancestor.parent
        }
        val activity = activity(view(viewport.fetchSemanticsNode()).context)
        val window = dialogWindow ?: activity.window
        val decor = window.decorView
        val location = IntArray(2).also(decor::getLocationOnScreen)
        val resources = view.resources
        val metrics = resources.displayMetrics
        val insets = checkNotNull(ViewCompat.getRootWindowInsets(decor)) { "Window insets unavailable" }
        val result =
            JSONObject()
                .put("type", if (dialogWindow == null) "activity" else "dialog")
                .put(
                    "decorScreenBounds",
                    rect(
                        Rect(
                            location[0].toFloat(),
                            location[1].toFloat(),
                            (location[0] + decor.width).toFloat(),
                            (location[1] + decor.height).toFloat(),
                        ),
                        metrics.density,
                    ),
                ).put(
                    "activityWindowBounds",
                    androidRect(activity.windowManager.currentWindowMetrics.bounds, metrics.density),
                ).put(
                    "maximumActivityWindowBounds",
                    androidRect(activity.windowManager.maximumWindowMetrics.bounds, metrics.density),
                ).put("osDensityDpi", activity.resources.configuration.densityDpi)
                .put("osDensity", activity.resources.displayMetrics.density)
                .put("osFontScale", activity.resources.configuration.fontScale)
                .put("resourceDensityDpi", metrics.densityDpi)
                .put("resourceDensity", metrics.density)
                .put("resourceFontScale", resources.configuration.fontScale)
                .put("resourceLocales", resources.configuration.locales.toLanguageTags())
                .put("resourceUiMode", resources.configuration.uiMode)
                .put("navigationModeRaw", Settings.Secure.getString(view.context.contentResolver, "navigation_mode"))
                .put("nativeOverrideSizeDensity", "Requires independent host physical/override readback")
        view.display?.mode?.let {
            result.put("physicalDisplayModePx", JSONArray(listOf(it.physicalWidth, it.physicalHeight)))
        }
        listOf(
            "systemBars" to WindowInsetsCompat.Type.systemBars(),
            "cutout" to WindowInsetsCompat.Type.displayCutout(),
            "safeDrawing" to (
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime()
            ),
        ).forEach { (name, type) ->
            val inset = insets.getInsets(type)
            result.put(
                name,
                JSONObject()
                    .put("px", JSONArray(listOf(inset.left, inset.top, inset.right, inset.bottom)))
                    .put(
                        "dp",
                        JSONArray(
                            listOf(inset.left, inset.top, inset.right, inset.bottom).map { it / metrics.density },
                        ),
                    ),
            )
        }
        return result
    }

    private fun view(node: SemanticsNode): View = checkNotNull((node.root as? ViewRootForTest)?.view)

    private fun activity(context: Context): Activity =
        generateSequence(context) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().first()

    private fun rect(
        bounds: Rect,
        density: Float,
    ): JSONObject =
        JSONObject()
            .put("px", JSONArray(listOf(bounds.left, bounds.top, bounds.right, bounds.bottom)))
            .put("dp", JSONArray(listOf(bounds.left, bounds.top, bounds.right, bounds.bottom).map { it / density }))
            .put("widthDp", bounds.width / density)
            .put("heightDp", bounds.height / density)

    private fun androidRect(
        bounds: android.graphics.Rect,
        density: Float,
    ): JSONObject =
        rect(
            Rect(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat()),
            density,
        )

    private fun assertContained(
        label: String,
        bounds: Rect,
        region: Rect,
    ) {
        assertTrue("$label outside viewport: $bounds / $region", contained(bounds, region))
    }

    private fun contained(
        bounds: Rect,
        region: Rect,
    ): Boolean =
        bounds.left >= region.left - 1f && bounds.right <= region.right + 1f &&
            bounds.top >= region.top - 1f && bounds.bottom <= region.bottom + 1f

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
