package com.insaner.fonecheck.ui.screens.biometrics

import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Assert.assertEquals
import org.junit.Test

class BiometricPresentationPolicyTest {
    @Test
    fun `authentication outcomes map to the shared semantic tones`() {
        assertEquals(SemanticTone.PASS, authResultTone(AuthResult.SUCCESS))
        assertEquals(SemanticTone.ATTENTION, authResultTone(AuthResult.LOCKED_OUT))
        assertEquals(SemanticTone.NEUTRAL, authResultTone(AuthResult.NOT_RECOGNIZED))
        assertEquals(SemanticTone.NEUTRAL, authResultTone(AuthResult.ERROR))
    }

    @Test
    fun `non verdict authentication states remain neutral`() {
        listOf(
            AuthResult.NONE,
            AuthResult.IN_PROGRESS,
            AuthResult.CANCELLED,
            AuthResult.NO_ENROLLMENT,
            AuthResult.UNAVAILABLE,
        ).forEach { result ->
            assertEquals(result.name, SemanticTone.NEUTRAL, authResultTone(result))
        }
    }
}
