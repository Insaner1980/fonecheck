package com.insaner.fonecheck.ui.screens.biometrics

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
fun BiometricTestScreen(
    modifier: Modifier = Modifier,
    viewModel: BiometricTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity
    var activePrompt by remember { mutableStateOf<BiometricPrompt?>(null) }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.cancelAuthentication()
            activePrompt?.cancelAuthentication()
            activePrompt = null
        }
    }

    fun authenticate() {
        if (activity == null) return
        viewModel.startAuthentication()
        if (!viewModel.state.value.promptActive) return
        runCatching {
            showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    viewModel.onAuthSuccess()
                    activePrompt = null
                },
                onFailed = viewModel::onAuthFailed,
                onError = { errorCode, message ->
                    viewModel.onAuthError(errorCode, message)
                    activePrompt = null
                },
            )
        }.onSuccess { activePrompt = it }
            .onFailure {
                viewModel.onAuthError(
                    BiometricPrompt.ERROR_VENDOR,
                    it.message.orEmpty(),
                )
            }
    }

    TestScreenContent(modifier = modifier) {
        item {
            TestSectionCard(
                icon = stringResource(R.string.biometric_capability_icon),
                title = stringResource(R.string.biometric_capabilities_title),
                statusText = capabilityStatusLabel(state.capability),
                statusColor =
                    when {
                        state.capability.strongAvailable -> Green400
                        state.capability.weakAvailable -> Yellow400
                        else -> Neutral500
                    },
                isExpanded = state.expandedSection == BiometricSection.CAPABILITIES,
                onClick = { viewModel.toggleSection(BiometricSection.CAPABILITIES) },
            ) {
                CapabilitiesDetails(state.capability)
            }
        }

        item {
            TestSectionCard(
                icon = stringResource(R.string.biometric_auth_icon),
                title = stringResource(R.string.biometric_test_auth),
                statusText = authResultLabel(state.authResult),
                statusColor = authResultColor(state.authResult),
                isExpanded = state.expandedSection == BiometricSection.AUTH_TEST,
                onClick = { viewModel.toggleSection(BiometricSection.AUTH_TEST) },
            ) {
                AuthTestSection(
                    state = state,
                    onAuthenticate = ::authenticate,
                )
            }
        }
    }
}

@Composable
private fun CapabilitiesDetails(capability: BiometricCapability) {
    SectionBox {
        DetailInfoRow(
            stringResource(R.string.biometric_fingerprint_hardware),
            yesNoLabel(capability.fingerprintHardware),
            valueColor = if (capability.fingerprintHardware) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.biometric_face_hardware),
            yesNoLabel(capability.faceHardware),
            valueColor = if (capability.faceHardware) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.biometric_strong),
            biometricAvailabilityLabel(capability.strongStatus),
            valueColor = if (capability.strongAvailable) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.biometric_weak),
            biometricAvailabilityLabel(capability.weakStatus),
            valueColor = if (capability.weakAvailable) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.biometric_device_credential),
            yesNoLabel(capability.deviceCredentialAvailable),
            valueColor = if (capability.deviceCredentialAvailable) Green400 else Neutral500,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.biometric_credential_excluded),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthTestSection(
    state: BiometricTestState,
    onAuthenticate: () -> Unit,
) {
    SectionBox {
        if (state.authResult != AuthResult.NONE) {
            Text(
                text = authResultDescription(state),
                style = MaterialTheme.typography.bodyMedium,
                color = authResultColor(state.authResult),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = stringResource(R.string.biometric_success_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onAuthenticate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Blue400),
            shape = RoundedCornerShape(8.dp),
            enabled = state.capability.weakAvailable && !state.promptActive,
        ) {
            Text(
                stringResource(
                    if (state.authResult.isTerminal) {
                        R.string.biometric_retry
                    } else {
                        R.string.biometric_test_auth
                    },
                ),
            )
        }
        if (!state.capability.weakAvailable) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = biometricAvailabilityLabel(state.capability.weakStatus),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
        }
    }
}

@Composable
private fun capabilityStatusLabel(capability: BiometricCapability): String =
    when {
        capability.strongAvailable -> stringResource(R.string.biometric_strong)
        capability.weakAvailable -> stringResource(R.string.biometric_weak)
        else -> biometricAvailabilityLabel(capability.weakStatus)
    }

@Composable
private fun biometricAvailabilityLabel(status: BiometricAvailability): String =
    stringResource(
        when (status) {
            BiometricAvailability.AVAILABLE -> R.string.biometric_available
            BiometricAvailability.NO_HARDWARE -> R.string.biometric_no_hardware
            BiometricAvailability.HARDWARE_UNAVAILABLE -> R.string.biometric_hardware_unavailable
            BiometricAvailability.NONE_ENROLLED -> R.string.biometric_none_enrolled
            BiometricAvailability.SECURITY_UPDATE_REQUIRED -> R.string.biometric_security_update_required
            BiometricAvailability.UNSUPPORTED -> R.string.biometric_unsupported
            BiometricAvailability.UNKNOWN -> R.string.biometric_unknown
        },
    )

@Composable
private fun authResultLabel(result: AuthResult): String =
    stringResource(
        when (result) {
            AuthResult.NONE -> R.string.biometric_ready
            AuthResult.IN_PROGRESS -> R.string.biometric_in_progress
            AuthResult.NOT_RECOGNIZED -> R.string.biometric_not_recognized
            AuthResult.SUCCESS -> R.string.biometric_auth_success
            AuthResult.CANCELLED -> R.string.biometric_cancelled
            AuthResult.LOCKED_OUT -> R.string.biometric_locked_out
            AuthResult.NO_ENROLLMENT -> R.string.biometric_none_enrolled
            AuthResult.UNAVAILABLE -> R.string.biometric_hardware_unavailable
            AuthResult.ERROR -> R.string.biometric_error
        },
    )

@Composable
private fun authResultDescription(state: BiometricTestState): String =
    if (state.authResult == AuthResult.ERROR && !state.authErrorMessage.isNullOrBlank()) {
        stringResource(R.string.biometric_auth_error, state.authErrorMessage)
    } else {
        authResultLabel(state.authResult)
    }

@Composable
private fun yesNoLabel(value: Boolean): String =
    stringResource(if (value) R.string.status_yes else R.string.status_no)

private fun authResultColor(result: AuthResult) =
    when (result) {
        AuthResult.SUCCESS -> Green400
        AuthResult.NOT_RECOGNIZED,
        AuthResult.LOCKED_OUT,
        -> Yellow400
        AuthResult.ERROR -> Red400
        AuthResult.NONE,
        AuthResult.IN_PROGRESS,
        AuthResult.CANCELLED,
        AuthResult.NO_ENROLLMENT,
        AuthResult.UNAVAILABLE,
        -> Neutral500
    }
