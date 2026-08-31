package com.insaner.fonecheck.ui.theme

import androidx.compose.ui.graphics.Color

// Instrument palette. Both themes are fully custom; Material dynamic color is never used.
// The ratio in each comment is the measured WCAG contrast against the background of the same ramp.

internal val HousingLight = Color(0xFF6F6A5B)
internal val HousingDark = Color(0xFF1D2220)

// Light ramp. A warmer instrument-paper base, near-black ink, three muted levels below it.
internal val PaperLight = Color(0xFFD8D2BD)
internal val InkLight = Color(0xFF17191C) // 11.64:1
internal val InkLight2 = Color(0xFF454844) // 6.13:1
internal val InkLight3 = Color(0xFF4B4D46) // 5.66:1
internal val InkLight4 = Color(0xFF8D8A7E) // 2.29:1, disabled content only, never meaningful text
internal val RuleLight = Color(0xFFB7B19F)
internal val TrackLight = Color(0xFFBDB8A8)
internal val PassLight = Color(0xFF13593E) // 5.49:1
internal val AttentionLight = Color(0xFF69480C) // 5.48:1
internal val AttentionFillLight = Color(0xFF75500D) // 4.77:1
internal val FailLight = Color(0xFF91271E) // 5.52:1

// Dark ramp. Near-black but not pure black, off-white ink, the same three muted levels.
internal val PaperDark = Color(0xFF0B0C0E)
internal val InkDark = Color(0xFFE8EAED) // 16.24:1
internal val InkDark2 = Color(0xFFB3B9C1) // 9.90:1
internal val InkDark3 = Color(0xFFA5ABB3) // 8.46:1
internal val InkDark4 = Color(0xFF4A4E55) // 2.34:1, disabled content only, never meaningful text
internal val RuleDark = Color(0xFF24272C)
internal val TrackDark = Color(0xFF2A2E34)
internal val PassDark = Color(0xFF3FB98A) // 7.94:1
internal val AttentionDark = Color(0xFFE8B04B) // 10.01:1
internal val AttentionFillDark = Color(0xFFE8A33D) // 9.07:1
internal val FailDark = Color(0xFFEB8881) // 7.80:1

// Instrument panel roles. Chromatic lamps keep the same identity in both palettes, while neutral
// lamp material follows its adjacent panel.
internal val PanelLight = Color(0xFFC6C1AE)
internal val PanelAltLight = Color(0xFFBDB8A5)
internal val EdgeLight = Color(0xFF17180F)
internal val BezelLight = Color(0xFF6F6A5B)
internal val PanelRuleLight = Color(0xFF9D9886)
internal val WindowBackgroundLight = Color(0xFF17180F)
internal val WindowFrameLight = Color(0xFF8B8675)
internal val WindowOffLight = Color(0xFF2A2D1D)
internal val WindowTrackLight = Color(0xFF33362A)
internal val LampUnlitLight = Color(0xFF3A382E)
internal val LampUnlitInkLight = Color(0xFFADAA9C)
internal val RowPassLight = Color(0xFF3F6B18)
internal val RowFaultLight = Color(0xFF9C3510)
internal val RowNotedLight = Color(0xFF7A5A05)
internal val RowUnlitLight = Color(0xFF6B6754)

internal val PanelDark = Color(0xFF343B38)
internal val PanelAltDark = Color(0xFF3C4340)
internal val EdgeDark = Color(0xFF10130F)
internal val BezelDark = Color(0xFF1B211E)
internal val PanelRuleDark = Color(0xFF4E5652)
internal val WindowBackgroundDark = Color(0xFF0D100E)
internal val WindowFrameDark = Color(0xFF5E665F)
internal val WindowOffDark = Color(0xFF232A22)
internal val WindowTrackDark = Color(0xFF2E352C)
internal val LampUnlitDark = Color(0xFF262C29)
internal val LampUnlitInkDark = Color(0xFF8D9A93)
internal val RowPassDark = Color(0xFFA0C95E)
internal val RowFaultDark = Color(0xFFE8795A)
internal val RowNotedDark = Color(0xFFE9B53A)
internal val RowUnlitDark = Color(0xFF93A099)

internal val WindowText = Color(0xFFE8ECD4)
internal val WindowDim = Color(0xFF7F8A6A)
internal val PrimaryActionBackground = Color(0xFFCF4F24)
internal val PrimaryActionContent = Color(0xFF0F0400)
internal val LampPass = Color(0xFF8FB851)
internal val LampPassInk = Color(0xFF1C3407)
internal val LampFault = Color(0xFFD32F2F)
internal val LampFaultInk = Color(0xFF0F0400)
internal val LampNoted = Color(0xFFE3AB26)
internal val LampNotedInk = Color(0xFF3A2703)
internal val LampInfoLight = Color(0xFFADA695)
internal val LampInfoDark = Color(0xFFB9B3A2)
internal val LampInfoInk = Color(0xFF23241C)

// Interactive Display test stimuli are intentionally outside the app-chrome palette. Their
// colours and geometry are part of the test input and must not change with the theme.
val Green400 = Color(0xFF62D991)
val Yellow400 = Color(0xFFF0C75E)
