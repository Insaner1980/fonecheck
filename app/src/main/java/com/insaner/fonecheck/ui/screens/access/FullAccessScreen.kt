package com.insaner.fonecheck.ui.screens.access

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.localization.diagnosticCategoryStringRes
import com.insaner.fonecheck.navigation.FULL_CHECK_FEATURE_ID
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent

@Composable
fun FullAccessScreen(
    featureId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feature = fullAccessFeature(featureId)
    TestScreenContent(modifier = modifier) {
        item {
            Column {
                SectionHeader(stringResource(feature.titleResId))
                Note(stringResource(feature.descriptionResId))
            }
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.full_access_title))
                Note(stringResource(R.string.full_access_offer))
                Note(stringResource(R.string.home_intro_free))
            }
        }
        item {
            SecondaryButton(
                label = stringResource(R.string.full_access_return),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private data class FullAccessFeature(
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
)

private fun fullAccessFeature(featureId: String): FullAccessFeature {
    if (featureId == FULL_CHECK_FEATURE_ID) {
        return FullAccessFeature(R.string.full_check_title, R.string.full_access_full_check_description)
    }
    val category = DiagnosticCategoryId.entries.firstOrNull { it.stableId == featureId }
    return FullAccessFeature(
        titleResId = category?.let(::diagnosticCategoryStringRes) ?: R.string.full_access_title,
        descriptionResId = category?.fullAccessDescriptionResId() ?: R.string.full_access_generic_description,
    )
}

@StringRes
private fun DiagnosticCategoryId.fullAccessDescriptionResId(): Int =
    when (this) {
        DiagnosticCategoryId.PERFORMANCE -> R.string.full_access_performance_description
        DiagnosticCategoryId.SIM -> R.string.full_access_sim_description
        DiagnosticCategoryId.AUDIO -> R.string.full_access_audio_description
        DiagnosticCategoryId.CAMERA -> R.string.full_access_camera_description
        DiagnosticCategoryId.CONNECTIVITY -> R.string.full_access_connectivity_description
        DiagnosticCategoryId.THERMAL -> R.string.full_access_thermal_description
        DiagnosticCategoryId.STORAGE -> R.string.full_access_storage_description
        DiagnosticCategoryId.VIBRATION -> R.string.full_access_vibration_description
        DiagnosticCategoryId.BUTTONS -> R.string.full_access_buttons_description
        DiagnosticCategoryId.BIOMETRICS -> R.string.full_access_biometrics_description
        DiagnosticCategoryId.DEVICE,
        DiagnosticCategoryId.DISPLAY,
        DiagnosticCategoryId.SENSORS,
        DiagnosticCategoryId.BATTERY,
        -> R.string.full_access_generic_description
    }
