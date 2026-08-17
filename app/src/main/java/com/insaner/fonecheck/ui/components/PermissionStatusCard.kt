package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun PermissionStatusCard(
    state: PermissionState,
    rationale: String,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        SectionHeader(stringResource(R.string.permission_section_title))
        StatusText(
            text = permissionStatusText(state),
            tone = permissionStatusTone(state),
        )

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

private fun permissionStatusTone(state: PermissionState): SemanticTone =
    when (state) {
        PermissionState.GRANTED -> SemanticTone.PASS
        PermissionState.DENIED,
        PermissionState.SETTINGS_RECOVERY,
        PermissionState.PARTIAL,
        -> SemanticTone.ATTENTION
        PermissionState.NOT_REQUESTED,
        PermissionState.NOT_REQUIRED,
        PermissionState.HARDWARE_ABSENT,
        -> SemanticTone.NEUTRAL
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
