package com.insaner.fonecheck.ui.components

import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.domain.observation.ObservationState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationReasonNoteTest {
    @Test
    fun `not-measured reason is hidden only when the value explains the state`() {
        val classification =
            ObservationClassification(
                state = ObservationState.NOT_MEASURED,
                reason = ObservationReason.TEST_NOT_RUN,
            )

        assertTrue(shouldShowObservationReason(classification))
        assertFalse(
            shouldShowObservationReason(
                classification = classification,
                valueExplainsNotMeasuredState = true,
            ),
        )
    }

    @Test
    fun `noted reason stays visible even when the value is descriptive`() {
        val classification =
            ObservationClassification(
                state = ObservationState.NOTED,
                reason = ObservationReason.ROOT_ARTIFACT_PRESENT,
            )

        assertTrue(
            shouldShowObservationReason(
                classification = classification,
                valueExplainsNotMeasuredState = true,
            ),
        )
    }
}
