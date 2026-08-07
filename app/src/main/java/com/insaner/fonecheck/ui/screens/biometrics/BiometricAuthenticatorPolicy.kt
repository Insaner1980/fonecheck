package com.insaner.fonecheck.ui.screens.biometrics

import androidx.biometric.BiometricManager.Authenticators

object BiometricAuthenticatorPolicy {
    const val ALLOWED_AUTHENTICATORS: Int = Authenticators.BIOMETRIC_WEAK
}
