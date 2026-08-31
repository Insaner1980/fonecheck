package com.insaner.fonecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colour roles for the instrument surface, read through [FonecheckTheme.colors].
 *
 * The base is near-monochrome: a background, primary ink and three muted levels below it. Colour
 * distinguishes diagnostic status from the primary action accent.
 *
 * [attention] and [attentionFill] are the two renderings of the single attention hue.
 * [attention] is the text-safe variant; [attentionFill] is reserved for filled diagnostic shapes
 * that carry no text on top of them.
 */
@Suppress("LongParameterList", "kotlin:S107") // A colour-role container is a flat list by nature.
@Immutable
data class FonecheckColors(
    val housing: Color,
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val ruleHairline: Color,
    val pass: Color,
    val attention: Color,
    val fail: Color,
    val attentionFill: Color,
    val segmentTrack: Color,
    val primaryButtonBackground: Color,
    val primaryButtonContent: Color,
    val primaryActionBackground: Color,
    val primaryActionContent: Color,
    val panel: Color,
    val panelAlt: Color,
    val edge: Color,
    val bezel: Color,
    val rule: Color,
    val windowBg: Color,
    val windowFrame: Color,
    val windowText: Color,
    val windowDim: Color,
    val windowOff: Color,
    val windowTrack: Color,
    val lampPass: Color,
    val lampPassInk: Color,
    val lampFault: Color,
    val lampFaultInk: Color,
    val lampNoted: Color,
    val lampNotedInk: Color,
    val lampInfo: Color,
    val lampInfoInk: Color,
    val lampUnlit: Color,
    val lampUnlitInk: Color,
    val rowPass: Color,
    val rowFault: Color,
    val rowNoted: Color,
    val rowUnlit: Color,
) {
    /** Full-weight rule under a section label. Always the primary ink of the same ramp. */
    val ruleStrong: Color get() = textPrimary
}

internal val LightFonecheckColors =
    FonecheckColors(
        housing = HousingLight,
        background = PaperLight,
        textPrimary = InkLight,
        textSecondary = InkLight2,
        textMuted = InkLight3,
        textDisabled = InkLight4,
        ruleHairline = RuleLight,
        pass = PassLight,
        attention = AttentionLight,
        fail = FailLight,
        attentionFill = AttentionFillLight,
        segmentTrack = TrackLight,
        // A near-black button on warm paper.
        primaryButtonBackground = InkLight,
        primaryButtonContent = PaperLight,
        primaryActionBackground = PrimaryActionBackground,
        primaryActionContent = PrimaryActionContent,
        panel = PanelLight,
        panelAlt = PanelAltLight,
        edge = EdgeLight,
        bezel = BezelLight,
        rule = PanelRuleLight,
        windowBg = WindowBackgroundLight,
        windowFrame = WindowFrameLight,
        windowText = WindowText,
        windowDim = WindowDim,
        windowOff = WindowOffLight,
        windowTrack = WindowTrackLight,
        lampPass = LampPass,
        lampPassInk = LampPassInk,
        lampFault = LampFault,
        lampFaultInk = LampFaultInk,
        lampNoted = LampNoted,
        lampNotedInk = LampNotedInk,
        lampInfo = LampInfoLight,
        lampInfoInk = LampInfoInk,
        lampUnlit = LampUnlitLight,
        lampUnlitInk = LampUnlitInkLight,
        rowPass = RowPassLight,
        rowFault = RowFaultLight,
        rowNoted = RowNotedLight,
        rowUnlit = RowUnlitLight,
    )

internal val DarkFonecheckColors =
    FonecheckColors(
        housing = HousingDark,
        background = PaperDark,
        textPrimary = InkDark,
        textSecondary = InkDark2,
        textMuted = InkDark3,
        textDisabled = InkDark4,
        ruleHairline = RuleDark,
        pass = PassDark,
        attention = AttentionDark,
        fail = FailDark,
        attentionFill = AttentionFillDark,
        segmentTrack = TrackDark,
        primaryButtonBackground = InkDark,
        primaryButtonContent = PaperDark,
        primaryActionBackground = PrimaryActionBackground,
        primaryActionContent = PrimaryActionContent,
        panel = PanelDark,
        panelAlt = PanelAltDark,
        edge = EdgeDark,
        bezel = BezelDark,
        rule = PanelRuleDark,
        windowBg = WindowBackgroundDark,
        windowFrame = WindowFrameDark,
        windowText = WindowText,
        windowDim = WindowDim,
        windowOff = WindowOffDark,
        windowTrack = WindowTrackDark,
        lampPass = LampPass,
        lampPassInk = LampPassInk,
        lampFault = LampFault,
        lampFaultInk = LampFaultInk,
        lampNoted = LampNoted,
        lampNotedInk = LampNotedInk,
        lampInfo = LampInfoDark,
        lampInfoInk = LampInfoInk,
        lampUnlit = LampUnlitDark,
        lampUnlitInk = LampUnlitInkDark,
        rowPass = RowPassDark,
        rowFault = RowFaultDark,
        rowNoted = RowNotedDark,
        rowUnlit = RowUnlitDark,
    )

internal val LocalFonecheckColors = staticCompositionLocalOf { DarkFonecheckColors }
