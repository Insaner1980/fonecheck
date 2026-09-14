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
    fun promptActivityIsDerivedFromEveryAuthenticationResult() {
        val expectedActivity =
            mapOf(
                AuthResult.NONE to false,
                AuthResult.IN_PROGRESS to true,
                AuthResult.NOT_RECOGNIZED to true,
                AuthResult.SUCCESS to false,
                AuthResult.CANCELLED to false,
                AuthResult.LOCKED_OUT to false,
                AuthResult.NO_ENROLLMENT to false,
                AuthResult.UNAVAILABLE to false,
                AuthResult.ERROR to false,
            )

        assertEquals(AuthResult.entries.toSet(), expectedActivity.keys)
        expectedActivity.forEach { (result, expected) ->
            val state = BiometricTestState(authResult = result)
            assertEquals(result.name, expected, state.promptActive)
            assertEquals(result.name, expected, BiometricTestState().copy(authResult = result).promptActive)
        }
    }

    @Test
    fun authenticationStartsExpandedWithoutLaunchingPromptAndCanBeCollapsed() {
        val viewModel = BiometricTestViewModel(FakeBiometricCapabilityProvider(BiometricCapability()))

        assertEquals(BiometricSection.AUTH_TEST, viewModel.state.value.expandedSection)
        assertEquals(AuthResult.NONE, viewModel.state.value.authResult)
        assertFalse(viewModel.state.value.promptActive)
        viewModel.toggleSection(BiometricSection.AUTH_TEST)
        assertEquals(null, viewModel.state.value.expandedSection)
        viewModel.toggleSection(BiometricSection.CAPABILITIES)
        assertEquals(BiometricSection.CAPABILITIES, viewModel.state.value.expandedSection)
    }

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
    fun unavailableCapabilitiesRejectPromptLaunch() {
        BiometricAvailability.entries.filter { it != BiometricAvailability.AVAILABLE }.forEach { status ->
            val viewModel =
                BiometricTestViewModel(
                    FakeBiometricCapabilityProvider(
                        BiometricCapability(
                            fingerprintHardware = true,
                            weakStatus = status,
                        ),
                    ),
                )

            assertFalse(status.name, viewModel.startAuthentication())

            val expected =
                if (status == BiometricAvailability.NONE_ENROLLED) AuthResult.NO_ENROLLMENT else AuthResult.UNAVAILABLE
            assertEquals(status.name, expected, viewModel.state.value.authResult)
            assertFalse(status.name, viewModel.state.value.promptActive)
        }
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
        val terminalState = viewModel.state.value
        viewModel.onAuthSuccess()
        viewModel.onAuthFailed()
        viewModel.onAuthError(BiometricPrompt.ERROR_LOCKOUT, "late lockout")

        assertEquals(terminalState, viewModel.state.value)
        assertEquals(AuthResult.SUCCESS, viewModel.state.value.authResult)
        assertFalse(viewModel.state.value.promptActive)
        assertEquals(1, viewModel.state.value.failedAttempts)
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
                BiometricPrompt.ERROR_CANCELED to AuthResult.UNAVAILABLE,
                BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED to AuthResult.UNAVAILABLE,
                BiometricPrompt.ERROR_VENDOR to AuthResult.ERROR,
            )

        cases.forEach { (errorCode, expected) ->
            val viewModel = availableViewModel()
            viewModel.startAuthentication()
            viewModel.onAuthFailed()
            viewModel.onAuthFailed()
            assertEquals(AuthResult.NOT_RECOGNIZED, viewModel.state.value.authResult)
            assertTrue(viewModel.state.value.promptActive)
            assertEquals(2, viewModel.state.value.failedAttempts)
            viewModel.onAuthError(errorCode, "message")

            assertEquals(expected, viewModel.state.value.authResult)
            assertFalse(viewModel.state.value.promptActive)
            assertEquals(2, viewModel.state.value.failedAttempts)
            assertEquals("message".takeIf { expected == AuthResult.ERROR }, viewModel.state.value.authErrorMessage)
            val terminalState = viewModel.state.value
            viewModel.onAuthSuccess()
            viewModel.onAuthFailed()
            viewModel.onAuthError(BiometricPrompt.ERROR_VENDOR, "late error")
            viewModel.cancelAuthentication()
            viewModel.onPromptLaunchFailure()
            assertEquals(terminalState, viewModel.state.value)

            assertTrue(viewModel.startAuthentication())
            assertTrue(viewModel.state.value.promptActive)
            assertEquals(0, viewModel.state.value.failedAttempts)
            viewModel.onAuthSuccess()
            assertEquals(AuthResult.SUCCESS, viewModel.state.value.authResult)
        }
    }

    @Test
    fun explicitCancellationEndsAnActivePromptAndIgnoresLateCallback() {
        val viewModel = availableViewModel()
        viewModel.startAuthentication()

        viewModel.cancelAuthentication()
        val terminalState = viewModel.state.value
        viewModel.onAuthSuccess()
        viewModel.onAuthFailed()
        viewModel.onAuthError(BiometricPrompt.ERROR_CANCELED, "late cancellation")

        assertEquals(terminalState, viewModel.state.value)
        assertEquals(AuthResult.CANCELLED, viewModel.state.value.authResult)
        assertFalse(viewModel.state.value.promptActive)
    }

    @Test
    fun promptLaunchFailureDoesNotRetainRawExceptionText() {
        val viewModel = availableViewModel()
        viewModel.startAuthentication()

        viewModel.onPromptLaunchFailure()

        assertEquals(AuthResult.ERROR, viewModel.state.value.authResult)
        assertEquals(null, viewModel.state.value.authErrorMessage)
        assertFalse(viewModel.state.value.promptActive)
        val terminalState = viewModel.state.value
        viewModel.onAuthSuccess()
        viewModel.onAuthFailed()
        viewModel.onAuthError(BiometricPrompt.ERROR_VENDOR, "late error")
        assertEquals(terminalState, viewModel.state.value)
    }

    @Test
    fun anActiveAuthenticationCannotBeStartedAgain() {
        val viewModel = availableViewModel()
        assertTrue(viewModel.startAuthentication())
        assertEquals(AuthResult.IN_PROGRESS, viewModel.state.value.authResult)
        assertTrue(viewModel.state.value.promptActive)
        val startedState = viewModel.state.value
        assertFalse(viewModel.startAuthentication())
        assertEquals(startedState, viewModel.state.value)
        viewModel.onAuthFailed()
        val activeState = viewModel.state.value

        assertFalse(viewModel.startAuthentication())

        assertEquals(activeState, viewModel.state.value)
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
