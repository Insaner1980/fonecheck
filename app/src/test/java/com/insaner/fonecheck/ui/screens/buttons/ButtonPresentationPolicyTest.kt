package com.insaner.fonecheck.ui.screens.buttons

import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Assert.assertEquals
import org.junit.Test

class ButtonPresentationPolicyTest {
    @Test
    fun `only completed and timed out phases carry verdict tones`() {
        assertEquals(SemanticTone.PASS, buttonStatusTone(ButtonTestPhase.COMPLETED))
        assertEquals(SemanticTone.ATTENTION, buttonStatusTone(ButtonTestPhase.TIMED_OUT))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.IDLE))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.RUNNING))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.SKIPPED))
    }
}
