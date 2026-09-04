package com.insaner.fonecheck.ui.screens.biometrics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BiometricAuthenticationActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun authenticationActionRequiresAReadyPromptAndNoActiveAuthentication() {
        var promptReady by mutableStateOf(false)
        var state by
            mutableStateOf(
                BiometricTestState(
                    capability = BiometricCapability(weakStatus = BiometricAvailability.AVAILABLE),
                ),
            )
        composeRule.setContent {
            FonecheckTheme {
                BiometricAuthTestSection(
                    state = state,
                    promptReady = promptReady,
                    onAuthenticate = {},
                )
            }
        }

        composeRule.onNodeWithTag("biometric_authenticate").assertIsNotEnabled()

        promptReady = true
        composeRule.onNodeWithTag("biometric_authenticate").assertIsEnabled()

        state = state.copy(authResult = AuthResult.IN_PROGRESS, promptActive = true)
        composeRule.onNodeWithTag("biometric_authenticate").assertIsNotEnabled()
    }
}
