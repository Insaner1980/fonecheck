package com.insaner.fonecheck.ui.screens.biometrics

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class AuthResult {
    NONE,
    IN_PROGRESS,
    NOT_RECOGNIZED,
    SUCCESS,
    CANCELLED,
    LOCKED_OUT,
    NO_ENROLLMENT,
    UNAVAILABLE,
    ERROR,
    ;

    val isTerminal: Boolean
        get() =
            this == SUCCESS ||
                this == CANCELLED ||
                this == LOCKED_OUT ||
                this == NO_ENROLLMENT ||
                this == UNAVAILABLE ||
                this == ERROR
}

enum class BiometricSection {
    CAPABILITIES,
    AUTH_TEST,
}

data class BiometricTestState(
    val capability: BiometricCapability = BiometricCapability(),
    val authResult: AuthResult = AuthResult.NONE,
    val authErrorMessage: String? = null,
    val promptActive: Boolean = false,
    val failedAttempts: Int = 0,
    val expandedSection: BiometricSection? = null,
)

@HiltViewModel
class BiometricTestViewModel
    @Inject
    constructor(
        capabilityProvider: BiometricCapabilityProvider,
    ) : ViewModel() {
        private val _state = MutableStateFlow(BiometricTestState(capability = capabilityProvider.read()))
        val state: StateFlow<BiometricTestState> = _state.asStateFlow()

        fun toggleSection(section: BiometricSection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
        }

        fun startAuthentication() {
            if (!canAuthenticate()) {
                _state.value =
                    _state.value.copy(
                        authResult = unavailableResult(_state.value.capability.weakStatus),
                        authErrorMessage = null,
                        promptActive = false,
                        failedAttempts = 0,
                    )
                return
            }
            _state.value =
                _state.value.copy(
                    authResult = AuthResult.IN_PROGRESS,
                    authErrorMessage = null,
                    promptActive = true,
                    failedAttempts = 0,
                )
        }

        fun onAuthSuccess() {
            complete(AuthResult.SUCCESS)
        }

        fun onAuthFailed() {
            if (!_state.value.promptActive || _state.value.authResult.isTerminal) return
            _state.value =
                _state.value.copy(
                    authResult = AuthResult.NOT_RECOGNIZED,
                    authErrorMessage = null,
                    failedAttempts = _state.value.failedAttempts + 1,
                )
        }

        fun onAuthError(
            errorCode: Int,
            errorMessage: String,
        ) {
            if (!_state.value.promptActive || _state.value.authResult.isTerminal) return
            complete(
                result = biometricAuthResult(errorCode),
                errorMessage = errorMessage,
            )
        }

        fun onPromptLaunchFailure() {
            complete(AuthResult.ERROR)
        }

        fun cancelAuthentication() {
            if (!_state.value.promptActive || _state.value.authResult.isTerminal) return
            complete(AuthResult.CANCELLED)
        }

        fun canAuthenticate(): Boolean = _state.value.capability.weakAvailable

        private fun complete(
            result: AuthResult,
            errorMessage: String? = null,
        ) {
            if (!_state.value.promptActive || _state.value.authResult.isTerminal) return
            _state.value =
                _state.value.copy(
                    authResult = result,
                    authErrorMessage = errorMessage.takeIf { result == AuthResult.ERROR },
                    promptActive = false,
                )
        }

        private fun unavailableResult(status: BiometricAvailability): AuthResult =
            if (status == BiometricAvailability.NONE_ENROLLED) {
                AuthResult.NO_ENROLLMENT
            } else {
                AuthResult.UNAVAILABLE
            }
    }

fun biometricAuthResult(errorCode: Int): AuthResult =
    when (errorCode) {
        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        BiometricPrompt.ERROR_USER_CANCELED,
        -> AuthResult.CANCELLED

        BiometricPrompt.ERROR_LOCKOUT,
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
        -> AuthResult.LOCKED_OUT

        BiometricPrompt.ERROR_NO_BIOMETRICS -> AuthResult.NO_ENROLLMENT
        BiometricPrompt.ERROR_HW_NOT_PRESENT,
        BiometricPrompt.ERROR_HW_UNAVAILABLE,
        BiometricPrompt.ERROR_CANCELED,
        BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED,
        -> AuthResult.UNAVAILABLE

        else -> AuthResult.ERROR
    }
