package com.insaner.fonecheck.ui.screens.biometrics

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.insaner.fonecheck.R

// This prompt diagnoses the authenticator and does not unlock data or authorize an operation.
@Suppress("kotlin:S6293")
internal fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailed: () -> Unit,
    onError: (errorCode: Int, message: String) -> Unit,
): BiometricPrompt {
    val callback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                onFailed()
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                onError(errorCode, errString.toString())
            }
        }
    val prompt =
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            callback,
        )
    val promptInfo =
        BiometricPrompt.PromptInfo
            .Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_title))
            .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
            .setAllowedAuthenticators(BiometricAuthenticatorPolicy.ALLOWED_AUTHENTICATORS)
            .setNegativeButtonText(activity.getString(R.string.biometric_prompt_cancel))
            .setConfirmationRequired(false)
            .build()
    prompt.authenticate(promptInfo)
    return prompt
}
