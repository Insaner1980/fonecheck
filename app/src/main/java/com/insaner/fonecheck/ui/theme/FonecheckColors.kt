package com.insaner.fonecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colour roles for the instrument surface, read through [FonecheckTheme.colors].
 *
 * The base is near-monochrome: a background, primary ink and three muted levels below it. Colour
 * appears only when it carries meaning — a status, or the one primary action on a screen.
 *
 * [attention] and [accentFill] are the two renderings of the single accent hue. [attention] is the
 * text-safe variant; [accentFill] is reserved for filled shapes that carry no text on top of them.
 */
@Suppress("LongParameterList", "kotlin:S107") // A colour-role container is a flat list by nature.
@Immutable
data class FonecheckColors(
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val ruleHairline: Color,
    val pass: Color,
    val attention: Color,
    val fail: Color,
    val accentFill: Color,
    val segmentTrack: Color,
    val primaryButtonBackground: Color,
    val primaryButtonContent: Color,
) {
    /** Full-weight rule under a section label. Always the primary ink of the same ramp. */
    val ruleStrong: Color get() = textPrimary
}

internal val LightFonecheckColors =
    FonecheckColors(
        background = PaperLight,
        textPrimary = InkLight,
        textSecondary = InkLight2,
        textMuted = InkLight3,
        textDisabled = InkLight4,
        ruleHairline = RuleLight,
        pass = PassLight,
        attention = AttentionLight,
        fail = FailLight,
        accentFill = AccentFillLight,
        segmentTrack = TrackLight,
        // A near-black button on warm paper. The accent would not carry enough weight here.
        primaryButtonBackground = InkLight,
        primaryButtonContent = PaperLight,
    )

internal val DarkFonecheckColors =
    FonecheckColors(
        background = PaperDark,
        textPrimary = InkDark,
        textSecondary = InkDark2,
        textMuted = InkDark3,
        textDisabled = InkDark4,
        ruleHairline = RuleDark,
        pass = PassDark,
        attention = AttentionDark,
        fail = FailDark,
        accentFill = AccentFillDark,
        segmentTrack = TrackDark,
        // A near-black button would disappear into the background, so the accent carries the action.
        primaryButtonBackground = AccentFillDark,
        primaryButtonContent = OnAccentDark,
    )

internal val LocalFonecheckColors = staticCompositionLocalOf { DarkFonecheckColors }
