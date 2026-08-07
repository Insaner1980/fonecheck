package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
fun PermissionStatusCard(
    state: PermissionState,
    rationale: String,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionBox(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(
                text = permissionStatusText(state),
                color =
                    when (state) {
                        PermissionState.GRANTED -> Green400
                        PermissionState.DENIED,
                        PermissionState.SETTINGS_RECOVERY,
                        PermissionState.PARTIAL,
                        -> Yellow400
                        else -> Neutral500
                    },
            )

            if (
                state == PermissionState.NOT_REQUESTED ||
                state == PermissionState.DENIED ||
                state == PermissionState.SETTINGS_RECOVERY ||
                state == PermissionState.PARTIAL
            ) {
                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when (state) {
                PermissionState.NOT_REQUESTED -> PermissionButton(
                    text = stringResource(R.string.permission_action_allow),
                    onClick = onRequest,
                )
                PermissionState.DENIED,
                PermissionState.PARTIAL,
                -> PermissionButton(
                    text = stringResource(R.string.permission_action_retry),
                    onClick = onRequest,
                )
                PermissionState.SETTINGS_RECOVERY -> PermissionButton(
                    text = stringResource(R.string.permission_action_open_settings),
                    onClick = onOpenSettings,
                )
                PermissionState.GRANTED,
                PermissionState.NOT_REQUIRED,
                PermissionState.HARDWARE_ABSENT,
                -> Unit
            }
        }
    }
}

@Composable
private fun PermissionButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(text)
    }
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
