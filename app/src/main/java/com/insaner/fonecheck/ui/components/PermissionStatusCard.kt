package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.classification.classifyPermission
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
fun PermissionStatusCard(
    state: PermissionState,
    rationale: String,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val classification = classifyPermission(state)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        // Nested inside a screen section, so it uses the panel rule rather than the panel edge.
        SectionHeader(
            label = stringResource(R.string.permission_section_title),
            ruleColor = FonecheckTheme.colors.rule,
            ruleThickness = FonecheckTheme.spacing.ruleThickness,
        )
        StatusText(
            text = permissionStatusText(state),
            tone = classification.toSemanticTone(),
        )
        ObservationReasonNote(classification)

        if (state.showsRationale()) {
            Note(rationale)
        }

        when (state) {
            PermissionState.NOT_REQUESTED ->
                PermissionButton(
                    text = stringResource(R.string.permission_action_allow),
                    onClick = onRequest,
                )
            PermissionState.DENIED ->
                PermissionButton(
                    text = stringResource(R.string.permission_action_retry),
                    onClick = onRequest,
                )
            PermissionState.SETTINGS_RECOVERY,
            PermissionState.PARTIAL,
            ->
                PermissionButton(
                    text = stringResource(R.string.permission_action_open_settings),
                    onClick = onOpenSettings,
                )
            PermissionState.GRANTED,
            PermissionState.NOT_REQUIRED,
            PermissionState.HARDWARE_ABSENT,
            -> Unit
        }
        HairlineRule()
    }
}

private fun PermissionState.showsRationale(): Boolean =
    when (this) {
        PermissionState.NOT_REQUESTED,
        PermissionState.DENIED,
        PermissionState.SETTINGS_RECOVERY,
        PermissionState.PARTIAL,
        -> true
        PermissionState.GRANTED,
        PermissionState.NOT_REQUIRED,
        PermissionState.HARDWARE_ABSENT,
        -> false
    }

@Composable
private fun PermissionButton(
    text: String,
    onClick: () -> Unit,
) {
    PrimaryButton(
        label = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun permissionStatusText(state: PermissionState): String =
    stringResource(
        when (state) {
            PermissionState.NOT_REQUESTED -> R.string.permission_status_not_requested
            PermissionState.GRANTED -> R.string.permission_status_granted
            PermissionState.DENIED -> R.string.permission_status_denied
            PermissionState.SETTINGS_RECOVERY -> R.string.permission_status_settings_recovery
            PermissionState.NOT_REQUIRED -> R.string.permission_status_not_required
            PermissionState.HARDWARE_ABSENT -> R.string.permission_status_hardware_absent
            PermissionState.PARTIAL -> R.string.permission_status_partial
        },
    )
