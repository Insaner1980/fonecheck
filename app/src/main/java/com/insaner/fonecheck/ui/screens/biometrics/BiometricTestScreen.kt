package com.insaner.fonecheck.ui.screens.biometrics

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun BiometricTestScreen(
    modifier: Modifier = Modifier,
    viewModel: BiometricTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity
    var biometricPrompt by remember { mutableStateOf<BiometricPrompt?>(null) }

    DisposableEffect(activity, viewModel) {
        val currentActivity = activity
        if (currentActivity == null) {
            biometricPrompt = null
            return@DisposableEffect onDispose {}
        }
        val prompt =
            createBiometricPrompt(
                activity = currentActivity,
                onSuccess = viewModel::onAuthSuccess,
                onFailed = viewModel::onAuthFailed,
                onError = viewModel::onAuthError,
            )
        biometricPrompt = prompt

        onDispose {
            if (!currentActivity.isChangingConfigurations) {
                viewModel.cancelAuthentication()
                prompt.cancelAuthentication()
            }
            if (biometricPrompt === prompt) {
                biometricPrompt = null
            }
        }
    }

    fun authenticate() {
        val currentActivity = activity ?: return
        val currentPrompt = biometricPrompt ?: return
        viewModel.startAuthentication()
        if (!viewModel.state.value.promptActive) return
        runCatching {
            authenticateWithBiometricPrompt(
                activity = currentActivity,
                prompt = currentPrompt,
            )
        }.onFailure {
            viewModel.onAuthError(
                BiometricPrompt.ERROR_VENDOR,
                it.message.orEmpty(),
            )
        }
    }

    TestScreenContent(modifier = modifier) {
        item {
            BiometricDisclosureSection(
                label = stringResource(R.string.biometric_capabilities_title),
                summary = capabilityStatusLabel(state.capability),
                tone = SemanticTone.NEUTRAL,
                expanded = state.expandedSection == BiometricSection.CAPABILITIES,
                onToggle = { viewModel.toggleSection(BiometricSection.CAPABILITIES) },
            ) {
                CapabilitiesDetails(state.capability)
            }
        }

        item {
            BiometricDisclosureSection(
                label = stringResource(R.string.biometric_test_auth),
                summary = authResultLabel(state.authResult),
                tone = authResultTone(state.authResult),
                expanded = state.expandedSection == BiometricSection.AUTH_TEST,
                onToggle = { viewModel.toggleSection(BiometricSection.AUTH_TEST) },
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
private fun BiometricDisclosureSection(
    label: String,
    summary: String,
    tone: SemanticTone,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        DisclosureHeader(
            label = label,
            summary = summary,
            expanded = expanded,
            onClick = onToggle,
            tone = tone,
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = FonecheckTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CapabilitiesDetails(capability: BiometricCapability) {
    Column {
        DataRow(
            label = stringResource(R.string.biometric_fingerprint_hardware),
            value = yesNoLabel(capability.fingerprintHardware),
        )
        DataRow(
            label = stringResource(R.string.biometric_face_hardware),
            value = yesNoLabel(capability.faceHardware),
        )
        DataRow(
            label = stringResource(R.string.biometric_strong),
            value = biometricAvailabilityLabel(capability.strongStatus),
        )
        DataRow(
            label = stringResource(R.string.biometric_weak),
            value = biometricAvailabilityLabel(capability.weakStatus),
        )
        DataRow(
            label = stringResource(R.string.biometric_device_credential),
            value = yesNoLabel(capability.deviceCredentialAvailable),
        )
        Note(text = stringResource(R.string.biometric_credential_excluded))
    }
}

@Composable
private fun AuthTestSection(
    state: BiometricTestState,
    onAuthenticate: () -> Unit,
) {
    when {
        state.authResult == AuthResult.ERROR && !state.authErrorMessage.isNullOrBlank() ->
            Note(text = stringResource(R.string.biometric_auth_error, state.authErrorMessage.orEmpty()))

        state.authResult == AuthResult.NOT_RECOGNIZED ->
            Note(text = stringResource(R.string.biometric_nonterminal_guidance))
    }

    Note(text = stringResource(R.string.biometric_success_disclaimer))
    PrimaryButton(
        label =
            stringResource(
                if (state.authResult.isTerminal) {
                    R.string.biometric_retry
                } else {
                    R.string.biometric_test_auth
                },
            ),
        onClick = onAuthenticate,
        modifier = Modifier.fillMaxWidth(),
        enabled = state.capability.weakAvailable && !state.promptActive,
    )
    if (!state.capability.weakAvailable) {
        Note(text = biometricAvailabilityLabel(state.capability.weakStatus))
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

internal fun authResultTone(result: AuthResult): SemanticTone =
    when (result) {
        AuthResult.SUCCESS -> SemanticTone.PASS
        AuthResult.NOT_RECOGNIZED,
        AuthResult.LOCKED_OUT,
        -> SemanticTone.ATTENTION
        AuthResult.ERROR -> SemanticTone.FAIL
        AuthResult.NONE,
        AuthResult.IN_PROGRESS,
        AuthResult.CANCELLED,
        AuthResult.NO_ENROLLMENT,
        AuthResult.UNAVAILABLE,
        -> SemanticTone.NEUTRAL
    }

@Composable
private fun yesNoLabel(value: Boolean): String =
    stringResource(if (value) R.string.status_yes else R.string.status_no)
