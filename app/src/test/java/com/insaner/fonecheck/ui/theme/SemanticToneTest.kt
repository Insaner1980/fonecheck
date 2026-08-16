package com.insaner.fonecheck.ui.theme

import com.insaner.fonecheck.domain.model.DiagnosticStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SemanticToneTest {
    @Test
    fun `a verdict maps to its own tone`() {
        assertEquals(SemanticTone.PASS, DiagnosticStatus.PASS.toSemanticTone())
        assertEquals(SemanticTone.ATTENTION, DiagnosticStatus.WARNING.toSemanticTone())
        assertEquals(SemanticTone.FAIL, DiagnosticStatus.FAIL.toSemanticTone())
    }

    @Test
    fun `a status that reports no verdict stays neutral`() {
        assertEquals(SemanticTone.NEUTRAL, DiagnosticStatus.INFO.toSemanticTone())
        assertEquals(SemanticTone.NEUTRAL, DiagnosticStatus.NOT_AVAILABLE.toSemanticTone())
        assertEquals(SemanticTone.NEUTRAL, DiagnosticStatus.NOT_TESTED.toSemanticTone())
    }
}
