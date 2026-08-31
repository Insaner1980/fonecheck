package com.insaner.fonecheck.ui.theme

import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.domain.observation.ObservationState
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

    @Test
    fun `the four observation states have one shared tone mapping`() {
        assertEquals(SemanticTone.PASS, ObservationClassification(ObservationState.PASS).toSemanticTone())
        assertEquals(
            SemanticTone.FAIL,
            ObservationClassification(ObservationState.FAULT, ObservationReason.BATTERY_DEAD).toSemanticTone(),
        )
        assertEquals(
            SemanticTone.ATTENTION,
            ObservationClassification(ObservationState.NOTED, ObservationReason.BATTERY_OVERHEAT).toSemanticTone(),
        )
        assertEquals(
            SemanticTone.NEUTRAL,
            ObservationClassification(ObservationState.NOT_MEASURED, ObservationReason.PERMISSION_DENIED)
                .toSemanticTone(),
        )
    }
}
