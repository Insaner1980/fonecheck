package com.insaner.fonecheck.ui.screens.vibration

import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Assert.assertEquals
import org.junit.Test

class VibrationPresentationPolicyTest {
    @Test
    fun `only the confirmed motor result carries a verdict tone`() {
        assertEquals(SemanticTone.PASS, vibrationResultTone(VibrationMotorResult.FELT))
        assertEquals(SemanticTone.FAIL, vibrationResultTone(VibrationMotorResult.NOT_FELT))
        assertEquals(SemanticTone.NEUTRAL, vibrationResultTone(VibrationMotorResult.SKIPPED))
        assertEquals(SemanticTone.NEUTRAL, vibrationResultTone(null))
    }
}
