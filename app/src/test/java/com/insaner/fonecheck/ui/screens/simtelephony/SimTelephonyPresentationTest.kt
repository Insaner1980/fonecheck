package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimTelephonyPresentationTest {
    @Test
    fun emptySecondSlotOnSingleSimPhoneIsUnused() {
        assertTrue(
            isUnusedSlot(
                inventory = SimInventoryCode.SINGLE_SIM,
                slot = slot(state = SimSlotStateCode.ABSENT),
            ),
        )
    }

    @Test
    fun detailFreeNotReadySlotBesideSingleActiveSimIsUnused() {
        assertTrue(
            isUnusedSlot(
                inventory = SimInventoryCode.SINGLE_SIM,
                slot = slot(state = SimSlotStateCode.NOT_READY),
            ),
        )
    }

    @Test
    fun notReadySlotWithReadableDetailsKeepsItsReportedState() {
        assertFalse(
            isUnusedSlot(
                inventory = SimInventoryCode.SINGLE_SIM,
                slot =
                    slot(state = SimSlotStateCode.NOT_READY).copy(
                        formFactor = SimFormFactorCode.PHYSICAL,
                    ),
            ),
        )
    }

    private fun slot(state: SimSlotStateCode) =
        SimSlotInfo(
            slotIndex = 1,
            state = state,
            activity = SimActivityCode.INACTIVE,
            formFactor = SimFormFactorCode.UNKNOWN,
            operatorName = null,
            countryIso = null,
            networkType = NetworkGenerationCode.UNKNOWN,
        )
}
