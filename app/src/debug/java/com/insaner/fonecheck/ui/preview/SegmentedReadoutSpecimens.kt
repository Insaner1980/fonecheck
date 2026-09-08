package com.insaner.fonecheck.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.SegmentGeometry
import com.insaner.fonecheck.ui.components.ThermalHeadroomGauge
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.components.segmentedFigureParts
import com.insaner.fonecheck.ui.components.segmentedWidth
import com.insaner.fonecheck.ui.components.stackedRowLayout
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.formatUiScientificNumber
import com.insaner.fonecheck.ui.format.uiFileSize
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import java.util.Locale

// Historical digit geometry for comparison; both sides now use the production overflow policy.
internal val ReadoutBaseline =
    SegmentGeometry(
        digitAdvance = 0.82f,
        pointAdvance = 0.24f,
        horizontalInset = 1.05f,
        outerGap = 1.05f,
        bevel = 0.15f,
        pointRadius = 0.07f,
        pointOffset = 0f,
    )

internal val ReadoutCandidate = SegmentGeometry()

class ReadoutSampleProvider : PreviewParameterProvider<Int> {
    override val values = (0..28).asSequence()
}

// Two full phone widths side by side, never two squeezed half-width readouts.
@Preview(name = "380 light EN", widthDp = 760)
@Preview(name = "380 dark EN", widthDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "380 light FI", widthDp = 760, locale = "fi")
@Preview(name = "380 dark FI", widthDp = 760, locale = "fi", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReadoutComparison(
    @PreviewParameter(ReadoutSampleProvider::class) sample: Int,
) {
    FonecheckTheme {
        Row {
            ReadoutSpecimen(sample, candidate = false)
            ReadoutSpecimen(sample, candidate = true)
        }
    }
}

@Preview(name = "320 light 1.0", widthDp = 640, fontScale = 1f)
@Preview(name = "320 dark 1.0", widthDp = 640, fontScale = 1f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "320 light 1.3", widthDp = 640, fontScale = 1.3f)
@Preview(name = "320 dark 1.3", widthDp = 640, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "320 light 2.0 FI", widthDp = 640, fontScale = 2f, locale = "fi")
@Preview(
    name = "320 dark 2.0 FI",
    widthDp = 640,
    fontScale = 2f,
    locale = "fi",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ReadoutNarrowComparison(
    @PreviewParameter(ReadoutSampleProvider::class) sample: Int,
) {
    FonecheckTheme {
        Row {
            ReadoutSpecimen(sample, candidate = false, widthDp = 320)
            ReadoutSpecimen(sample, candidate = true, widthDp = 320)
        }
    }
}

/** Also used by the opt-in instrumented capture: one actual Compose panel per capture. */
@Composable
fun ReadoutSpecimen(
    sample: Int,
    candidate: Boolean,
    modifier: Modifier = Modifier,
    widthDp: Int = 380,
    language: String = LocalConfiguration.current.locales[0].language,
) {
    val locale = Locale.forLanguageTag(language)
    val configuration =
        Configuration(LocalConfiguration.current).apply { setLocale(locale) }
    val resources = LocalContext.current.createConfigurationContext(configuration).resources

    fun number(
        value: Number,
        digits: Int = 0,
    ) = formatUiNumber(value, locale, digits, digits)
    val (value, unit) =
        when (sample) {
            in 0..9 -> number(sample) to null
            10 -> number(9).padStart(2, '0') to resources.getString(R.string.home_latest_passed_total, number(14))
            11 -> number(17) to "/ ${number(25)}"
            12 -> number(44) to "/ ${number(255)}"
            13 -> number(100) to "%"
            14 -> number(1.25, 2) to "GB"
            15 -> number(25.7, 1) to resources.getString(R.string.storage_usage_unit)
            16 -> number(36.7, 1) to "°C"
            17 -> number(0.76, 2) to null
            18 -> number(-100) to null
            19 -> "+${number(100)}" to null
            20 -> number(255) to "/ ${number(255)}"
            21 -> number(-999.999, 3) to null
            22 -> number(-0.99999, 5) to null
            // SensorTestScreen formats magnitudes >= 1000 in fixed notation, without a length cap.
            23 -> number(-Float.MAX_VALUE, 1) to null
            24 -> formatUiScientificNumber(-Float.MIN_VALUE, locale, 2) to null
            25 -> number(Int.MAX_VALUE) to "/ ${number(Int.MAX_VALUE)}"
            26 -> {
                val parts = segmentedFigureParts(uiFileSize(Long.MAX_VALUE))!!
                parts.number to parts.suffix
            }
            27 -> number(100) to "/ ${number(100)}"
            else -> number(14) to resources.getString(R.string.home_latest_passed_total, number(14))
        }
    val geometry = if (candidate) ReadoutCandidate else ReadoutBaseline
    val style =
        when (sample) {
            10, 28 -> FonecheckTheme.type.readout.copy(fontSize = 56.sp, lineHeight = 60.sp)
            17 ->
                FonecheckTheme.type.readout.copy(
                    fontSize = FonecheckTheme.type.readout.fontSize * 0.75f,
                    lineHeight = FonecheckTheme.type.readout.lineHeight * 0.75f,
                )
            else -> FonecheckTheme.type.readout
        }
    val figureHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    val baselineWidth = figureHeight * segmentedWidth(value, ReadoutBaseline)
    val candidateWidth = figureHeight * segmentedWidth(value, ReadoutCandidate)
    val stacked = stackedRowLayout()
    val unitHeight =
        if (stacked && unit != null) {
            with(LocalDensity.current) {
                FonecheckTheme.type.readoutUnit.lineHeight
                    .toDp()
            }
        } else {
            0.dp
        }
    Column(
        modifier =
            modifier
                .width(widthDp.dp)
                .background(FonecheckTheme.colors.panel)
                .testTag("readout-specimen")
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        // Debug metadata, not product copy. Same space is reserved on both sides.
        Text(if (candidate) "Candidate" else "Baseline", color = FonecheckTheme.colors.textPrimary)
        Text(
            "$widthDp dp; ${LocalDensity.current.fontScale}; $language; sample $sample",
            style = FonecheckTheme.type.note,
            color = FonecheckTheme.colors.textPrimary,
        )
        // Reserve the same minimum height; actual stacking can differ between digit geometries.
        ReadoutWindow(
            modifier = Modifier.heightIn(min = figureHeight + unitHeight + FonecheckTheme.spacing.md * 2),
        ) {
            if (sample == 17) ThermalHeadroomGauge(headroom = 0.76f)
            WindowReading(
                value = value,
                unit = unit,
                style = style,
                geometry = geometry,
                stacked = stacked,
                modifier = Modifier.testTag("complete-reading"),
            )
        }
        // Expose natural widths, including the full width of a horizontally scrollable figure.
        Text(
            "Figure width B/C: ${number(baselineWidth.value, 1)} / ${number(candidateWidth.value, 1)} dp; " +
                "window interior: ${widthDp - 64} dp",
            style = FonecheckTheme.type.note,
            color = FonecheckTheme.colors.textPrimary,
        )
    }
}
