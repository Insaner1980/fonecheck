package com.insaner.fonecheck.ui.screens.vibration

import org.junit.Assert.assertEquals
import org.junit.Test

class VibrationCapabilityPolicyTest {
    @Test
    fun onlyDefiniteEffectSupportIsReported() {
        assertEquals(
            listOf(VibrationEffectCode.CLICK, VibrationEffectCode.TICK),
            VibrationCapabilityPolicy.supportedEffects(
                results = intArrayOf(1, 0, -1, 1),
                supportedValue = 1,
            ),
        )
    }

    @Test
    fun primitiveSupportPreservesTheDocumentedQueryOrder() {
        assertEquals(
            listOf(VibrationPrimitiveCode.CLICK, VibrationPrimitiveCode.SPIN),
            VibrationCapabilityPolicy.supportedPrimitives(
                booleanArrayOf(true, false, true, false, false, false, false, false),
            ),
        )
    }
}
