package com.insaner.fonecheck.ui.screens.biometrics

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

enum class BiometricAvailability(
    val stableCode: String,
) {
    AVAILABLE("available"),
    NO_HARDWARE("no_hardware"),
    HARDWARE_UNAVAILABLE("hardware_unavailable"),
    NONE_ENROLLED("none_enrolled"),
    SECURITY_UPDATE_REQUIRED("security_update_required"),
    UNSUPPORTED("unsupported"),
    UNKNOWN("unknown"),
}

data class BiometricCapability(
    val fingerprintHardware: Boolean = false,
    val faceHardware: Boolean = false,
    val strongStatus: BiometricAvailability = BiometricAvailability.UNKNOWN,
    val weakStatus: BiometricAvailability = BiometricAvailability.UNKNOWN,
    val deviceCredentialAvailable: Boolean = false,
) {
    val strongAvailable: Boolean get() = strongStatus == BiometricAvailability.AVAILABLE
    val weakAvailable: Boolean get() = weakStatus == BiometricAvailability.AVAILABLE
}

interface BiometricCapabilityProvider {
    fun read(): BiometricCapability
}

class AndroidBiometricCapabilityProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : BiometricCapabilityProvider {
        override fun read(): BiometricCapability {
            val packageManager = context.packageManager
            val biometricManager = BiometricManager.from(context)
            val keyguardManager = context.getSystemService(KeyguardManager::class.java)
            return BiometricCapability(
                fingerprintHardware = packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT),
                faceHardware =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        packageManager.hasSystemFeature(PackageManager.FEATURE_FACE),
                strongStatus =
                    biometricAvailability(
                        biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG),
                    ),
                weakStatus =
                    biometricAvailability(
                        biometricManager.canAuthenticate(Authenticators.BIOMETRIC_WEAK),
                    ),
                deviceCredentialAvailable = keyguardManager?.isDeviceSecure == true,
            )
        }
    }

fun biometricAvailability(result: Int): BiometricAvailability =
    when (result) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            BiometricAvailability.SECURITY_UPDATE_REQUIRED
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
        else -> BiometricAvailability.UNKNOWN
    }
