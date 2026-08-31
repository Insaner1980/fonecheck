package com.insaner.fonecheck.ui.screens.buttons

import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ButtonPresentationPolicyTest {
    @Test
    fun `reset is offered only after a run has produced state`() {
        assertFalse(buttonResetAvailable(ButtonTestPhase.IDLE))
        assertFalse(buttonResetAvailable(ButtonTestPhase.RUNNING))
        assertTrue(buttonResetAvailable(ButtonTestPhase.COMPLETED))
        assertTrue(buttonResetAvailable(ButtonTestPhase.TIMED_OUT))
        assertTrue(buttonResetAvailable(ButtonTestPhase.SKIPPED))
    }

    @Test
    fun `only completed phase carries a verdict tone`() {
        assertEquals(SemanticTone.PASS, buttonStatusTone(ButtonTestPhase.COMPLETED))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.TIMED_OUT))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.IDLE))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.RUNNING))
        assertEquals(SemanticTone.NEUTRAL, buttonStatusTone(ButtonTestPhase.SKIPPED))
    }
}
