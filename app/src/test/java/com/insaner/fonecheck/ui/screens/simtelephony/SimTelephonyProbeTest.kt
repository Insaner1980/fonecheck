package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimTelephonyProbeTest {
    @Test
    fun inventorySeparatesNoTelephonyNoSimInactiveSingleMultipleAndUnknown() {
        assertEquals(
            SimInventoryCode.NO_TELEPHONY,
            SimTelephonyProbe.inventory(hasTelephonyHardware = false, slots = emptyList()),
        )
        assertEquals(
            SimInventoryCode.NO_SIM,
            SimTelephonyProbe.inventory(
                hasTelephonyHardware = true,
                slots = listOf(slot(state = SimSlotStateCode.ABSENT, activity = SimActivityCode.INACTIVE)),
            ),
        )
        assertEquals(
            SimInventoryCode.INACTIVE_SIM,
            SimTelephonyProbe.inventory(
                hasTelephonyHardware = true,
                slots = listOf(slot(state = SimSlotStateCode.PIN_REQUIRED, activity = SimActivityCode.INACTIVE)),
            ),
        )
        assertEquals(
            SimInventoryCode.SINGLE_SIM,
            SimTelephonyProbe.inventory(
                hasTelephonyHardware = true,
                slots = listOf(slot(state = SimSlotStateCode.READY, activity = SimActivityCode.ACTIVE)),
            ),
        )
        assertEquals(
            SimInventoryCode.MULTIPLE_SIM,
            SimTelephonyProbe.inventory(
                hasTelephonyHardware = true,
                slots =
                    listOf(
                        slot(state = SimSlotStateCode.READY, activity = SimActivityCode.ACTIVE),
                        slot(state = SimSlotStateCode.READY, activity = SimActivityCode.ACTIVE),
                    ),
            ),
        )
        assertEquals(
            SimInventoryCode.UNKNOWN,
            SimTelephonyProbe.inventory(
                hasTelephonyHardware = true,
                slots = listOf(slot(state = SimSlotStateCode.UNKNOWN, activity = SimActivityCode.UNKNOWN)),
            ),
        )
    }

    @Test
    fun deniedPermissionStillClassifiesReadySlotsWithoutSensitiveDetails() {
        val info =
            SimTelephonyProbe.fromSnapshot(
                SimTelephonySnapshot(
                    hasTelephonyHardware = true,
                    phoneStatePermissionGranted = false,
                    phoneCount = 1,
                    phoneTypeCode = 1,
                    dataNetworkTypeCode = 13,
                    slots =
                        listOf(
                            SimSlotSnapshot(
                                slotIndex = 0,
                                stateCode = 5,
                                activeSubscription = null,
                                embedded = true,
                                operatorName = "must-not-leak",
                                countryIso = "fi",
                                networkTypeCode = 13,
                            ),
                        ),
                ),
            )

        assertEquals(SimInventoryCode.SINGLE_SIM, info.inventory)
        assertFalse(info.phoneStatePermissionGranted)
        assertEquals(NetworkGenerationCode.UNKNOWN, info.dataNetworkType)
        assertEquals(SimActivityCode.ACTIVE, info.simSlots.single().activity)
        assertEquals(SimFormFactorCode.UNKNOWN, info.simSlots.single().formFactor)
        assertEquals(null, info.simSlots.single().operatorName)
        assertEquals(null, info.simSlots.single().countryIso)
    }

    @Test
    fun apiLevelGuardsModernModemCountAndEmbeddedSim() {
        var modernCalls = 0
        var legacyCalls = 0

        assertEquals(
            2,
            SimTelephonyProbe.modemCount(
                sdkInt = 30,
                activeModemCount = {
                    modernCalls += 1
                    2
                },
                legacyPhoneCount = {
                    legacyCalls += 1
                    1
                },
            ),
        )
        assertEquals(1, modernCalls)
        assertEquals(0, legacyCalls)

        assertEquals(
            1,
            SimTelephonyProbe.modemCount(
                sdkInt = 29,
                activeModemCount = {
                    modernCalls += 1
                    2
                },
                legacyPhoneCount = {
                    legacyCalls += 1
                    1
                },
            ),
        )
        assertEquals(1, modernCalls)
        assertEquals(1, legacyCalls)

        assertEquals(
            3,
            SimTelephonyProbe.modemCount(
                sdkInt = 30,
                activeModemCount = { error("OEM failure") },
                legacyPhoneCount = { 3 },
            ),
        )

        var embeddedRead = false
        assertEquals(
            SimFormFactorCode.UNKNOWN,
            SimTelephonyProbe.formFactor(sdkInt = 27) {
                embeddedRead = true
                true
            },
        )
        assertFalse(embeddedRead)
        assertEquals(
            SimFormFactorCode.EMBEDDED,
            SimTelephonyProbe.formFactor(sdkInt = 28) {
                embeddedRead = true
                true
            },
        )
        assertTrue(embeddedRead)
        assertEquals(
            SimFormFactorCode.UNKNOWN,
            SimTelephonyProbe.formFactor(sdkInt = 28) { error("OEM failure") },
        )
    }

    private fun slot(
        state: SimSlotStateCode,
        activity: SimActivityCode,
    ) = SimSlotInfo(
        slotIndex = 0,
        state = state,
        activity = activity,
        formFactor = SimFormFactorCode.UNKNOWN,
        operatorName = null,
        countryIso = null,
        networkType = NetworkGenerationCode.UNKNOWN,
    )
}
