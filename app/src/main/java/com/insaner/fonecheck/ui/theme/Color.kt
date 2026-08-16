package com.insaner.fonecheck.ui.theme

import androidx.compose.ui.graphics.Color

// Instrument palette. Both themes are fully custom; Material dynamic color is never used.
// The ratio in each comment is the measured WCAG contrast against the background of the same ramp.

// Light ramp. Warm off-white paper, near-black ink, three muted levels below it.
internal val PaperLight = Color(0xFFF7F6F3)
internal val InkLight = Color(0xFF17191C) // 16.30:1
internal val InkLight2 = Color(0xFF5C6066) // 5.85:1
internal val InkLight3 = Color(0xFF6B6F75) // 4.68:1
internal val InkLight4 = Color(0xFFA9A7A0) // 2.23:1 — disabled content only, never meaningful text
internal val RuleLight = Color(0xFFDFDDD7)
internal val TrackLight = Color(0xFFD5D3CC)
internal val PassLight = Color(0xFF166647) // 6.42:1
internal val AttentionLight = Color(0xFF95610F) // 4.86:1
internal val AccentFillLight = Color(0xFFB5761A) // 3.49:1 — fills only, never text
internal val FailLight = Color(0xFFA32C22) // 6.61:1

// Dark ramp. Near-black but not pure black, off-white ink, the same three muted levels.
internal val PaperDark = Color(0xFF0B0C0E)
internal val InkDark = Color(0xFFE8EAED) // 16.24:1
internal val InkDark2 = Color(0xFF9AA0A8) // 7.42:1
internal val InkDark3 = Color(0xFF7C828A) // 5.05:1
internal val InkDark4 = Color(0xFF4A4E55) // 2.34:1 — disabled content only, never meaningful text
internal val RuleDark = Color(0xFF24272C)
internal val TrackDark = Color(0xFF2A2E34)
internal val PassDark = Color(0xFF3FB98A) // 7.94:1
internal val AttentionDark = Color(0xFFE8B04B) // 10.01:1
internal val AccentFillDark = Color(0xFFE8A33D) // 9.07:1
internal val FailDark = Color(0xFFE8736B) // 6.61:1
internal val OnAccentDark = Color(0xFF1A1206) // 8.59:1 against AccentFillDark

// Legacy tokens. Screens still name these directly; each one is deleted in the task that migrates
// the last screen using it. Status colors resolve to the palette above through readableStatusColor.
val Aqua40 = Color(0xFF00716D)
val Aqua80 = Color(0xFF48D8D2)
val Blue400 = Aqua80
val Lavender40 = Color(0xFF6846A5)
val Lavender80 = Color(0xFFC9B2FF)
val Coral80 = Color(0xFFFFB4A9)

val Neutral950 = Color(0xFF0D0F14)
val Neutral900 = Color(0xFF15181F)
val Neutral850 = Color(0xFF1C2028)
val Neutral800 = Color(0xFF242A34)
val Neutral700 = Color(0xFF343B48)
val Neutral600 = Color(0xFF48515F)
val Neutral500 = Color(0xFF697383)
val Neutral400 = Color(0xFF8993A2)
val Neutral300 = Color(0xFFAEB6C4)
val Neutral200 = Color(0xFFD0D6E0)
val Neutral100 = Color(0xFFF4F6FA)
val Neutral50 = Color(0xFFFBFCFE)

val Green400 = Color(0xFF62D991)
val Yellow400 = Color(0xFFF0C75E)
val Red400 = Color(0xFFFF7474)
