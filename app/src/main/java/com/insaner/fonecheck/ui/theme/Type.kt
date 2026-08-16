package com.insaner.fonecheck.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.insaner.fonecheck.R

val DmSans =
    FontFamily(
        Font(R.font.dm_sans_regular, FontWeight.Normal),
        Font(R.font.dm_sans_medium, FontWeight.Medium),
        // Bold is registered only for screens that still set FontWeight.Bold inline. It is deleted
        // in the task that removes the last one; no role below uses a weight above Medium.
        Font(R.font.dm_sans_bold, FontWeight.Bold),
    )

val JetBrainsMono =
    FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )

/** Tabular figures, so a column of numbers stays aligned as the digits change. */
private const val TABULAR_FIGURES = "tnum"

/**
 * The type roles, read through [FonecheckTheme.type]. A neutral sans carries UI text and labels; a
 * monospace carries every measured value, number, unit, identifier and timestamp.
 */
@Immutable
object FonecheckType {
    val screenTitle =
        TextStyle(
            fontFamily = DmSans,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 28.sp,
        )

    /** Small, wide-tracked and uppercased by the component that draws it. */
    val sectionLabel =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.12.em,
        )

    val rowLabel =
        TextStyle(
            fontFamily = DmSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

    val rowValue =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFeatureSettings = TABULAR_FIGURES,
        )

    val readout =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontFeatureSettings = TABULAR_FIGURES,
        )

    /** The unit or denominator that sits beside a [readout]. */
    val readoutUnit =
        TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontFeatureSettings = TABULAR_FIGURES,
        )

    val note =
        TextStyle(
            fontFamily = DmSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )

    val buttonLabel =
        TextStyle(
            fontFamily = DmSans,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        )
}

// The Material slots are derived from the roles above so there is one type scale, not two. Slots
// with no role of their own exist only so unmigrated screens and Material components keep the app
// typeface; they are removed once no screen reads MaterialTheme.typography directly.
val Typography =
    Typography(
        displayLarge = FonecheckType.readout.copy(fontSize = 48.sp, lineHeight = 52.sp),
        displayMedium = FonecheckType.readout,
        displaySmall = FonecheckType.readout.copy(fontSize = 32.sp, lineHeight = 36.sp),
        headlineLarge = FonecheckType.screenTitle.copy(fontSize = 28.sp, lineHeight = 36.sp),
        headlineMedium = FonecheckType.screenTitle.copy(fontSize = 24.sp, lineHeight = 32.sp),
        headlineSmall = FonecheckType.screenTitle.copy(fontSize = 22.sp, lineHeight = 28.sp),
        titleLarge = FonecheckType.screenTitle,
        titleMedium = FonecheckType.screenTitle.copy(fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall = FonecheckType.screenTitle.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = FonecheckType.rowLabel.copy(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = FonecheckType.rowLabel,
        bodySmall = FonecheckType.note,
        labelLarge = FonecheckType.buttonLabel,
        labelMedium = FonecheckType.screenTitle.copy(fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = FonecheckType.sectionLabel,
    )
