package com.insaner.fonecheck.ui.screens.biometrics

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.insaner.fonecheck.ui.classification.classifyBiometric
import com.insaner.fonecheck.ui.classification.classifyBiometricCapability
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureSection
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
fun BiometricTestScreen(
    modifier: Modifier = Modifier,
    viewModel: BiometricTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
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

    TestScreenContent(modifier = modifier, liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis) {
        item {
            DisclosureSection(
                label = stringResource(R.string.biometric_capabilities_title),
                summary = capabilityStatusLabel(state.capability),
                tone = SemanticTone.NEUTRAL,
                expanded = state.expandedSection == BiometricSection.CAPABILITIES,
                onClick = { viewModel.toggleSection(BiometricSection.CAPABILITIES) },
            ) {
                CapabilitiesDetails(state.capability)
            }
        }

        item {
            val classification = classifyBiometric(state.authResult)
            DisclosureSection(
                label = stringResource(R.string.biometric_test_auth),
                summary = authResultLabel(state.authResult),
                tone = classification.toSemanticTone(),
                expanded = state.expandedSection == BiometricSection.AUTH_TEST,
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
    val strongClassification = classifyBiometricCapability(capability.strongStatus)
    val weakClassification = classifyBiometricCapability(capability.weakStatus)
    val sharedReason = strongClassification.reason?.takeIf { it == weakClassification.reason }
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
            showDivider = sharedReason != null || strongClassification.reason == null,
        )
        if (sharedReason == null) {
            ObservationReasonNote(strongClassification)
            if (strongClassification.reason != null) HairlineRule()
        }
        DataRow(
            label = stringResource(R.string.biometric_weak),
            value = biometricAvailabilityLabel(capability.weakStatus),
            showDivider = sharedReason == null && weakClassification.reason == null,
        )
        ObservationReasonNote(
            if (sharedReason != null) strongClassification else weakClassification,
        )
        if (sharedReason != null || weakClassification.reason != null) HairlineRule()
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
    ObservationReasonNote(classifyBiometric(state.authResult))
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
            AuthResult.NONE -> R.string.status_not_measured
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

internal fun authResultTone(result: AuthResult): SemanticTone = classifyBiometric(result).toSemanticTone()

@Composable
private fun yesNoLabel(value: Boolean): String = stringResource(if (value) R.string.status_yes else R.string.status_no)
