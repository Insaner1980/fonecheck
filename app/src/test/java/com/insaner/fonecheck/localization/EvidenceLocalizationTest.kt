package com.insaner.fonecheck.localization

import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class EvidenceLocalizationTest {
    @Test
    fun `all canonical reason codes have localized resources`() {
        val reasons =
            listOf(
                EvidenceReasonCode.PERMISSION_DENIED,
                EvidenceReasonCode.NOT_RUN,
                EvidenceReasonCode.SKIPPED,
                EvidenceReasonCode.CANCELLED,
                EvidenceReasonCode.TIMEOUT,
                EvidenceReasonCode.INSUFFICIENT_SPACE,
                EvidenceReasonCode.ERROR,
                EvidenceReasonCode.HARDWARE_UNAVAILABLE,
                EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
                EvidenceReasonCode.PLATFORM_RESTRICTION,
                EvidenceReasonCode.BIOMETRIC_LOCKOUT,
                EvidenceReasonCode.BIOMETRIC_NOT_ENROLLED,
                EvidenceReasonCode.DISABLED,
                EvidenceReasonCode.USER_CONFIRMED_FAILURE,
                EvidenceReasonCode.DEGRADED,
            )

        reasons.forEach { assertNotNull(evidenceReasonStringRes(it)) }
    }

    @Test
    fun `future reason codes use readable fallback`() {
        val futureReason = EvidenceReasonCode("future_vendor_reason")

        assertNull(evidenceReasonStringRes(futureReason))
        assertEquals("Future vendor reason", stableCodeDisplayText(futureReason.value))
    }

    @Test
    fun `report evidence labels and stable values resolve through resources`() {
        assertNotNull(evidenceLabelStringRes("battery.level"))
        assertNotNull(stableTextStringRes("app_cache"))
        assertNull(evidenceLabelStringRes("future.vendor_check"))
        assertNull(stableTextStringRes("future_vendor_value"))
    }
}
