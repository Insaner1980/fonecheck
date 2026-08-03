package com.insaner.fonecheck.ui.screens.biometrics

import android.app.Application
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// ── State classes ────────────────────────────────────────────────────────────────

data class BiometricCapability(
    val strongAvailable: Boolean = false,
    val weakAvailable: Boolean = false,
    val deviceCredentialAvailable: Boolean = false,
    val strongStatusMessage: String = "",
    val weakStatusMessage: String = "",
)

enum class AuthResult {
    NONE,
    SUCCESS,
    FAILED,
    ERROR,
}

enum class BiometricSection {
    CAPABILITIES,
    AUTH_TEST,
}

data class BiometricTestState(
    val capability: BiometricCapability = BiometricCapability(),
    val authResult: AuthResult = AuthResult.NONE,
    val authErrorMessage: String? = null,
    val expandedSection: BiometricSection? = null,
)

// ── ViewModel ────────────────────────────────────────────────────────────────────

@HiltViewModel
class BiometricTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()
        private val biometricManager = BiometricManager.from(context)

        private val _state = MutableStateFlow(BiometricTestState())
        val state: StateFlow<BiometricTestState> = _state

        init {
            checkCapabilities()
        }

        fun toggleSection(section: BiometricSection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
        }

        fun onAuthSuccess() {
            _state.value =
                _state.value.copy(
                    authResult = AuthResult.SUCCESS,
                    authErrorMessage = null,
                )
        }

        fun onAuthFailed() {
            _state.value =
                _state.value.copy(
                    authResult = AuthResult.FAILED,
                    authErrorMessage = null,
                )
        }

        fun onAuthError(errorMessage: String) {
            _state.value =
                _state.value.copy(
                    authResult = AuthResult.ERROR,
                    authErrorMessage = errorMessage,
                )
        }

        fun canAuthenticate(): Boolean =
            _state.value.capability.strongAvailable || _state.value.capability.weakAvailable

        private fun checkCapabilities() {
            val strongResult = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)
            val weakResult = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_WEAK)
            val credentialResult = biometricManager.canAuthenticate(Authenticators.DEVICE_CREDENTIAL)

            _state.value =
                _state.value.copy(
                    capability =
                        BiometricCapability(
                            strongAvailable = strongResult == BiometricManager.BIOMETRIC_SUCCESS,
                            weakAvailable = weakResult == BiometricManager.BIOMETRIC_SUCCESS,
                            deviceCredentialAvailable = credentialResult == BiometricManager.BIOMETRIC_SUCCESS,
                            strongStatusMessage = statusMessage(strongResult),
                            weakStatusMessage = statusMessage(weakResult),
                        ),
                )
        }

        private fun statusMessage(result: Int): String =
            when (result) {
                BiometricManager.BIOMETRIC_SUCCESS -> "Available"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No hardware"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware unavailable"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Not enrolled"
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Unknown"
                else -> "Unknown ($result)"
            }
    }
