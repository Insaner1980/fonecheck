package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
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

    @Test
    fun grantedPermissionNormalizesProtectedSlotDetails() {
        val info =
            SimTelephonyProbe.fromSnapshot(
                SimTelephonySnapshot(
                    hasTelephonyHardware = true,
                    phoneStatePermissionGranted = true,
                    phoneCount = -2,
                    phoneTypeCode = 2,
                    dataNetworkTypeCode = 20,
                    slots =
                        listOf(
                            SimSlotSnapshot(
                                slotIndex = 1,
                                stateCode = 4,
                                activeSubscription = false,
                                embedded = false,
                                operatorName = "  Example Mobile  ",
                                countryIso = " fi ",
                                networkTypeCode = 18,
                            ),
                            SimSlotSnapshot(
                                slotIndex = 2,
                                stateCode = 5,
                                activeSubscription = true,
                                embedded = true,
                                operatorName = "unknown",
                                countryIso = " ",
                                networkTypeCode = 3,
                            ),
                        ),
                ),
            )

        assertEquals(TelephonyHardwareCode.AVAILABLE, info.hardware)
        assertEquals(SimInventoryCode.SINGLE_SIM, info.inventory)
        assertEquals(0, info.phoneCount)
        assertEquals(PhoneTypeCode.CDMA, info.phoneType)
        assertEquals(NetworkGenerationCode.FIFTH_GENERATION, info.dataNetworkType)
        assertEquals(SimActivityCode.INACTIVE, info.simSlots[0].activity)
        assertEquals(SimFormFactorCode.PHYSICAL, info.simSlots[0].formFactor)
        assertEquals("Example Mobile", info.simSlots[0].operatorName)
        assertEquals("FI", info.simSlots[0].countryIso)
        assertEquals(NetworkGenerationCode.UNKNOWN, info.simSlots[0].networkType)
        assertEquals(SimActivityCode.ACTIVE, info.simSlots[1].activity)
        assertEquals(SimFormFactorCode.EMBEDDED, info.simSlots[1].formFactor)
        assertEquals(null, info.simSlots[1].operatorName)
        assertEquals(null, info.simSlots[1].countryIso)
        assertEquals(NetworkGenerationCode.THIRD_GENERATION, info.simSlots[1].networkType)
    }

    @Test
    fun snapshotsWithoutTelephonyPreserveUnknownFallbacks() {
        val info =
            SimTelephonyProbe.fromSnapshot(
                SimTelephonySnapshot(
                    hasTelephonyHardware = false,
                    phoneStatePermissionGranted = true,
                    phoneCount = 0,
                    phoneTypeCode = -1,
                    dataNetworkTypeCode = null,
                    slots =
                        listOf(
                            SimSlotSnapshot(
                                slotIndex = 0,
                                stateCode = -1,
                                activeSubscription = null,
                                embedded = null,
                                operatorName = null,
                                countryIso = null,
                                networkTypeCode = null,
                            ),
                        ),
                ),
            )

        assertEquals(TelephonyHardwareCode.NO_HARDWARE, info.hardware)
        assertEquals(SimInventoryCode.NO_TELEPHONY, info.inventory)
        assertEquals(PhoneTypeCode.UNKNOWN, info.phoneType)
        assertEquals(NetworkGenerationCode.UNKNOWN, info.dataNetworkType)
        assertEquals(SimActivityCode.UNKNOWN, info.simSlots.single().activity)
        assertEquals(SimFormFactorCode.UNKNOWN, info.simSlots.single().formFactor)
    }

    @Test
    fun `stable telephony codes map every documented value and unknown fallback`() {
        val simStates =
            mapOf(
                5 to SimSlotStateCode.READY,
                1 to SimSlotStateCode.ABSENT,
                4 to SimSlotStateCode.NETWORK_LOCKED,
                2 to SimSlotStateCode.PIN_REQUIRED,
                3 to SimSlotStateCode.PUK_REQUIRED,
                6 to SimSlotStateCode.NOT_READY,
                7 to SimSlotStateCode.PERMANENTLY_DISABLED,
                8 to SimSlotStateCode.CARD_IO_ERROR,
                9 to SimSlotStateCode.CARD_RESTRICTED,
                -1 to SimSlotStateCode.UNKNOWN,
            )
        simStates.forEach { (code, expected) -> assertEquals(expected, SimTelephonyProbe.simState(code)) }

        mapOf(
            1 to NetworkGenerationCode.SECOND_GENERATION,
            2 to NetworkGenerationCode.SECOND_GENERATION,
            4 to NetworkGenerationCode.SECOND_GENERATION,
            7 to NetworkGenerationCode.SECOND_GENERATION,
            11 to NetworkGenerationCode.SECOND_GENERATION,
            16 to NetworkGenerationCode.SECOND_GENERATION,
            3 to NetworkGenerationCode.THIRD_GENERATION,
            5 to NetworkGenerationCode.THIRD_GENERATION,
            6 to NetworkGenerationCode.THIRD_GENERATION,
            8 to NetworkGenerationCode.THIRD_GENERATION,
            9 to NetworkGenerationCode.THIRD_GENERATION,
            10 to NetworkGenerationCode.THIRD_GENERATION,
            12 to NetworkGenerationCode.THIRD_GENERATION,
            14 to NetworkGenerationCode.THIRD_GENERATION,
            15 to NetworkGenerationCode.THIRD_GENERATION,
            17 to NetworkGenerationCode.THIRD_GENERATION,
            13 to NetworkGenerationCode.FOURTH_GENERATION,
            18 to NetworkGenerationCode.UNKNOWN,
            20 to NetworkGenerationCode.FIFTH_GENERATION,
            -1 to NetworkGenerationCode.UNKNOWN,
        ).forEach { (code, expected) -> assertEquals(expected, SimTelephonyProbe.networkGeneration(code)) }
        assertEquals(NetworkGenerationCode.UNKNOWN, SimTelephonyProbe.networkGeneration(null))

        mapOf(
            1 to PhoneTypeCode.GSM,
            2 to PhoneTypeCode.CDMA,
            3 to PhoneTypeCode.SIP,
            0 to PhoneTypeCode.NONE,
            -1 to PhoneTypeCode.UNKNOWN,
        ).forEach { (code, expected) -> assertEquals(expected, SimTelephonyProbe.phoneType(code)) }
    }

    @Test
    fun modemAndFormFactorFailuresUseSafeFallbacks() {
        assertEquals(
            0,
            SimTelephonyProbe.modemCount(
                sdkInt = 30,
                activeModemCount = { error("modern failure") },
                legacyPhoneCount = { error("legacy failure") },
            ),
        )
        assertEquals(
            0,
            SimTelephonyProbe.modemCount(
                sdkInt = 29,
                activeModemCount = { 3 },
                legacyPhoneCount = { -1 },
            ),
        )
        assertEquals(SimFormFactorCode.PHYSICAL, SimTelephonyProbe.formFactor(sdkInt = 28) { false })
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
