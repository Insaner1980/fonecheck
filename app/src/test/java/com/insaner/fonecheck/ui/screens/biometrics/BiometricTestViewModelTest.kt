package com.insaner.fonecheck.ui.screens.biometrics

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricTestViewModelTest {
    @Test
    fun capabilityKeepsHardwareFeaturesAndAuthenticatorAvailabilitySeparate() {
        val capability =
            BiometricCapability(
                fingerprintHardware = true,
                faceHardware = false,
                strongStatus = BiometricAvailability.NONE_ENROLLED,
                weakStatus = BiometricAvailability.AVAILABLE,
                deviceCredentialAvailable = true,
            )

        val viewModel = BiometricTestViewModel(FakeBiometricCapabilityProvider(capability))

        assertEquals(capability, viewModel.state.value.capability)
        assertTrue(viewModel.canAuthenticate())
        assertFalse(viewModel.state.value.capability.strongAvailable)
        assertTrue(viewModel.state.value.capability.weakAvailable)
    }

    @Test
    fun promptPolicyAllowsBiometricsButNeverDeviceCredentialFallback() {
        assertEquals(
            Authenticators.BIOMETRIC_WEAK,
            BiometricAuthenticatorPolicy.ALLOWED_AUTHENTICATORS,
        )
        assertEquals(
            0,
            BiometricAuthenticatorPolicy.ALLOWED_AUTHENTICATORS and Authenticators.DEVICE_CREDENTIAL,
        )
    }

    @Test
    fun platformStatusCodesPreserveEnrollmentAndHardwareBoundaries() {
        assertEquals(
            BiometricAvailability.NONE_ENROLLED,
            biometricAvailability(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED),
        )
        assertEquals(
            BiometricAvailability.NO_HARDWARE,
            biometricAvailability(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE),
        )
        assertEquals(
            BiometricAvailability.HARDWARE_UNAVAILABLE,
            biometricAvailability(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE),
        )
        assertEquals(
            BiometricAvailability.AVAILABLE,
            biometricAvailability(BiometricManager.BIOMETRIC_SUCCESS),
        )
    }

    @Test
    fun missingEnrollmentEndsBeforePromptLaunch() {
        val viewModel =
            BiometricTestViewModel(
                FakeBiometricCapabilityProvider(
                    BiometricCapability(
                        fingerprintHardware = true,
                        weakStatus = BiometricAvailability.NONE_ENROLLED,
                    ),
                ),
            )

        viewModel.startAuthentication()

        assertEquals(AuthResult.NO_ENROLLMENT, viewModel.state.value.authResult)
        assertFalse(viewModel.state.value.promptActive)
    }

    @Test
    fun nonMatchIsNonterminalAndLaterSuccessCompletesExactlyOnce() {
        val viewModel = availableViewModel()
        viewModel.startAuthentication()

        viewModel.onAuthFailed()

        assertEquals(AuthResult.NOT_RECOGNIZED, viewModel.state.value.authResult)
        assertTrue(viewModel.state.value.promptActive)
        assertEquals(1, viewModel.state.value.failedAttempts)

        viewModel.onAuthSuccess()
        viewModel.onAuthError(BiometricPrompt.ERROR_LOCKOUT, "late lockout")

        assertEquals(AuthResult.SUCCESS, viewModel.state.value.authResult)
        assertFalse(viewModel.state.value.promptActive)
        assertEquals(null, viewModel.state.value.authErrorMessage)
    }

    @Test
    fun terminalPromptErrorsAreClassifiedAndCanBeRetried() {
        val cases =
            listOf(
                BiometricPrompt.ERROR_NEGATIVE_BUTTON to AuthResult.CANCELLED,
                BiometricPrompt.ERROR_USER_CANCELED to AuthResult.CANCELLED,
                BiometricPrompt.ERROR_LOCKOUT to AuthResult.LOCKED_OUT,
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT to AuthResult.LOCKED_OUT,
                BiometricPrompt.ERROR_NO_BIOMETRICS to AuthResult.NO_ENROLLMENT,
                BiometricPrompt.ERROR_HW_NOT_PRESENT to AuthResult.UNAVAILABLE,
                BiometricPrompt.ERROR_HW_UNAVAILABLE to AuthResult.UNAVAILABLE,
                BiometricPrompt.ERROR_VENDOR to AuthResult.ERROR,
            )

        cases.forEach { (errorCode, expected) ->
            val viewModel = availableViewModel()
            viewModel.startAuthentication()
            viewModel.onAuthError(errorCode, "message")

            assertEquals(expected, viewModel.state.value.authResult)
            assertFalse(viewModel.state.value.promptActive)

            viewModel.startAuthentication()
            viewModel.onAuthSuccess()
            assertEquals(AuthResult.SUCCESS, viewModel.state.value.authResult)
        }
    }

    @Test
    fun explicitCancellationEndsAnActivePromptAndIgnoresLateCallback() {
        val viewModel = availableViewModel()
        viewModel.startAuthentication()

        viewModel.cancelAuthentication()
        viewModel.onAuthError(BiometricPrompt.ERROR_CANCELED, "late cancellation")

        assertEquals(AuthResult.CANCELLED, viewModel.state.value.authResult)
        assertFalse(viewModel.state.value.promptActive)
    }

    private fun availableViewModel() =
        BiometricTestViewModel(
            FakeBiometricCapabilityProvider(
                BiometricCapability(
                    fingerprintHardware = true,
                    strongStatus = BiometricAvailability.AVAILABLE,
                    weakStatus = BiometricAvailability.AVAILABLE,
                ),
            ),
        )

    private class FakeBiometricCapabilityProvider(
        private val capability: BiometricCapability,
    ) : BiometricCapabilityProvider {
        override fun read(): BiometricCapability = capability
    }
}
