package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import java.util.Locale

data class SimSlotSnapshot(
    val slotIndex: Int,
    val stateCode: Int,
    val activeSubscription: Boolean?,
    val embedded: Boolean?,
    val operatorName: String?,
    val countryIso: String?,
    val networkTypeCode: Int?,
)

data class SimTelephonySnapshot(
    val hasTelephonyHardware: Boolean,
    val phoneStatePermissionGranted: Boolean,
    val phoneCount: Int,
    val phoneTypeCode: Int,
    val dataNetworkTypeCode: Int?,
    val slots: List<SimSlotSnapshot>,
    val sdkInt: Int = 36,
)

object SimTelephonyProbe {
    fun fromSnapshot(snapshot: SimTelephonySnapshot): SimTelephonyInfo {
        val slots =
            snapshot.slots.map { slot ->
                val state = simState(slot.stateCode)
                val mayReadProtectedDetails = snapshot.phoneStatePermissionGranted
                SimSlotInfo(
                    slotIndex = slot.slotIndex,
                    state = state,
                    activity = activity(slot.activeSubscription, state),
                    formFactor =
                        if (mayReadProtectedDetails && slot.embedded != null) {
                            formFactor(snapshot.sdkInt) { slot.embedded }
                        } else {
                            SimFormFactorCode.UNKNOWN
                        },
                    operatorName = slot.operatorName.takeIf { mayReadProtectedDetails }.normalizedText(),
                    countryIso =
                        slot.countryIso
                            .takeIf { mayReadProtectedDetails }
                            .normalizedText()
                            ?.uppercase(Locale.ROOT),
                    networkType =
                        if (mayReadProtectedDetails) {
                            networkGeneration(slot.networkTypeCode)
                        } else {
                            NetworkGenerationCode.UNKNOWN
                        },
                )
            }
        return SimTelephonyInfo(
            hardware =
                if (snapshot.hasTelephonyHardware) {
                    TelephonyHardwareCode.AVAILABLE
                } else {
                    TelephonyHardwareCode.NO_HARDWARE
                },
            inventory = inventory(snapshot.hasTelephonyHardware, slots),
            simSlots = slots,
            phoneType = phoneType(snapshot.phoneTypeCode),
            phoneCount = snapshot.phoneCount.coerceAtLeast(0),
            dataNetworkType =
                if (snapshot.phoneStatePermissionGranted) {
                    networkGeneration(snapshot.dataNetworkTypeCode)
                } else {
                    NetworkGenerationCode.UNKNOWN
                },
            phoneStatePermissionGranted = snapshot.phoneStatePermissionGranted,
        )
    }

    fun inventory(
        hasTelephonyHardware: Boolean,
        slots: List<SimSlotInfo>,
    ): SimInventoryCode =
        if (!hasTelephonyHardware) {
            SimInventoryCode.NO_TELEPHONY
        } else {
            val activeCount = slots.count { it.activity == SimActivityCode.ACTIVE }
            when {
                activeCount > 1 -> SimInventoryCode.MULTIPLE_SIM
                activeCount == 1 -> SimInventoryCode.SINGLE_SIM
                slots.isNotEmpty() && slots.all { it.state == SimSlotStateCode.ABSENT } -> SimInventoryCode.NO_SIM
                slots.any {
                    it.state != SimSlotStateCode.ABSENT && it.state != SimSlotStateCode.UNKNOWN
                } -> SimInventoryCode.INACTIVE_SIM
                else -> SimInventoryCode.UNKNOWN
            }
        }

    fun modemCount(
        sdkInt: Int,
        activeModemCount: () -> Int,
        legacyPhoneCount: () -> Int,
    ): Int {
        val count =
            if (sdkInt >= 30) {
                runCatching(activeModemCount).getOrElse {
                    runCatching(legacyPhoneCount).getOrDefault(0)
                }
            } else {
                runCatching(legacyPhoneCount).getOrDefault(0)
            }
        return count.coerceAtLeast(0)
    }

    fun formFactor(
        sdkInt: Int,
        isEmbedded: () -> Boolean,
    ): SimFormFactorCode =
        if (sdkInt < 28) {
            SimFormFactorCode.UNKNOWN
        } else {
            runCatching {
                if (isEmbedded()) SimFormFactorCode.EMBEDDED else SimFormFactorCode.PHYSICAL
            }.getOrDefault(SimFormFactorCode.UNKNOWN)
        }

    fun simState(code: Int): SimSlotStateCode =
        when (code) {
            5 -> SimSlotStateCode.READY
            1 -> SimSlotStateCode.ABSENT
            4 -> SimSlotStateCode.NETWORK_LOCKED
            2 -> SimSlotStateCode.PIN_REQUIRED
            3 -> SimSlotStateCode.PUK_REQUIRED
            6 -> SimSlotStateCode.NOT_READY
            7 -> SimSlotStateCode.PERMANENTLY_DISABLED
            8 -> SimSlotStateCode.CARD_IO_ERROR
            9 -> SimSlotStateCode.CARD_RESTRICTED
            else -> SimSlotStateCode.UNKNOWN
        }

    fun networkGeneration(code: Int?): NetworkGenerationCode =
        when (code) {
            1, 2, 4, 7, 11, 16 -> NetworkGenerationCode.SECOND_GENERATION
            3, 5, 6, 8, 9, 10, 12, 14, 15, 17 -> NetworkGenerationCode.THIRD_GENERATION
            13 -> NetworkGenerationCode.FOURTH_GENERATION
            20 -> NetworkGenerationCode.FIFTH_GENERATION
            else -> NetworkGenerationCode.UNKNOWN
        }

    fun phoneType(code: Int): PhoneTypeCode =
        when (code) {
            1 -> PhoneTypeCode.GSM
            2 -> PhoneTypeCode.CDMA
            3 -> PhoneTypeCode.SIP
            0 -> PhoneTypeCode.NONE
            else -> PhoneTypeCode.UNKNOWN
        }

    private fun activity(
        activeSubscription: Boolean?,
        state: SimSlotStateCode,
    ): SimActivityCode =
        when (activeSubscription) {
            true -> SimActivityCode.ACTIVE
            false -> SimActivityCode.INACTIVE
            null ->
                when (state) {
                    SimSlotStateCode.READY -> SimActivityCode.ACTIVE
                    SimSlotStateCode.UNKNOWN -> SimActivityCode.UNKNOWN
                    else -> SimActivityCode.INACTIVE
                }
        }

    private fun String?.normalizedText(): String? =
        this
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("unknown", ignoreCase = true) }
}
