package com.insaner.fonecheck.ui.screens.biometrics

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
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
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    TestScreenContent(modifier = modifier) {
        // Capabilities
        item {
            TestSectionCard(
                icon = "BIO",
                title = stringResource(R.string.biometric_capabilities_title),
                statusText =
                    when {
                        state.capability.strongAvailable -> stringResource(R.string.biometric_strong)
                        state.capability.weakAvailable -> stringResource(R.string.biometric_weak)
                        else -> stringResource(R.string.biometric_not_available)
                    },
                statusColor =
                    when {
                        state.capability.strongAvailable -> Green400
                        state.capability.weakAvailable -> Yellow400
                        else -> Red400
                    },
                isExpanded = state.expandedSection == BiometricSection.CAPABILITIES,
                onClick = { viewModel.toggleSection(BiometricSection.CAPABILITIES) },
            ) {
                CapabilitiesDetails(state.capability)
            }
        }

        // Authentication Test
        item {
            TestSectionCard(
                icon = "FPR",
                title = stringResource(R.string.biometric_test_auth),
                statusText =
                    when (state.authResult) {
                        AuthResult.NONE -> "Ready"
                        AuthResult.SUCCESS -> stringResource(R.string.biometric_auth_success)
                        AuthResult.FAILED -> stringResource(R.string.biometric_auth_failed)
                        AuthResult.ERROR -> "Error"
                    },
                statusColor =
                    when (state.authResult) {
                        AuthResult.NONE -> Neutral500
                        AuthResult.SUCCESS -> Green400
                        AuthResult.FAILED -> Red400
                        AuthResult.ERROR -> Red400
                    },
                isExpanded = state.expandedSection == BiometricSection.AUTH_TEST,
                onClick = { viewModel.toggleSection(BiometricSection.AUTH_TEST) },
            ) {
                AuthTestSection(
                    state = state,
                    onAuthenticate = {
                        val activity = context as? FragmentActivity ?: return@AuthTestSection
                        showBiometricPrompt(
                            activity = activity,
                            onSuccess = viewModel::onAuthSuccess,
                            onFailed = viewModel::onAuthFailed,
                            onError = { _, message -> viewModel.onAuthError(message) },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun CapabilitiesDetails(capability: BiometricCapability) {
    SectionBox {
        DetailInfoRow(
            stringResource(R.string.biometric_strong),
            capability.strongStatusMessage,
            valueColor = if (capability.strongAvailable) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.biometric_weak),
            capability.weakStatusMessage,
            valueColor = if (capability.weakAvailable) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.biometric_device_credential),
            if (capability.deviceCredentialAvailable) {
                stringResource(R.string.status_enabled)
            } else {
                stringResource(R.string.status_disabled)
            },
            valueColor = if (capability.deviceCredentialAvailable) Green400 else Neutral500,
        )
    }
}

@Composable
private fun AuthTestSection(
    state: BiometricTestState,
    onAuthenticate: () -> Unit,
) {
    SectionBox {
        when (state.authResult) {
            AuthResult.SUCCESS -> {
                Text(
                    text = stringResource(R.string.biometric_auth_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green400,
                )
            }
            AuthResult.FAILED -> {
                Text(
                    text = stringResource(R.string.biometric_auth_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Red400,
                )
            }
            AuthResult.ERROR -> {
                Text(
                    text =
                        stringResource(
                            R.string.biometric_auth_error,
                            state.authErrorMessage ?: "",
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Red400,
                )
            }
            AuthResult.NONE -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAuthenticate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Blue400),
            shape = RoundedCornerShape(8.dp),
            enabled = state.capability.strongAvailable || state.capability.weakAvailable,
        ) {
            Text(stringResource(R.string.biometric_test_auth))
        }

        if (!state.capability.strongAvailable && !state.capability.weakAvailable) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.biometric_not_available),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
        }
    }
}
