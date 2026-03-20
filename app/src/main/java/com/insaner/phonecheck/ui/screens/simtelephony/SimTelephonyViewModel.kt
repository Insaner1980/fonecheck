package com.insaner.phonecheck.ui.screens.simtelephony

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.insaner.phonecheck.domain.model.SimSlotInfo
import com.insaner.phonecheck.domain.model.SimTelephonyInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimTelephonyViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    val simTelephonyInfo: SimTelephonyInfo = gatherSimTelephonyInfo(application)

    private fun gatherSimTelephonyInfo(application: Application): SimTelephonyInfo {
        val telephonyManager = application.getSystemService(TelephonyManager::class.java)
        val subscriptionManager = application.getSystemService(SubscriptionManager::class.java)
        val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED

        val phoneCount = telephonyManager.phoneCount
        val isDualSim = phoneCount > 1

        val simSlots = gatherSimSlots(
            application,
            telephonyManager,
            subscriptionManager,
            phoneCount,
            hasPhoneStatePermission,
        )

        val dataNetworkType = if (hasPhoneStatePermission) {
            mapNetworkType(telephonyManager.dataNetworkType)
        } else {
            "Permission required"
        }

        return SimTelephonyInfo(
            simSlots = simSlots,
            isDualSim = isDualSim,
            phoneType = mapPhoneType(telephonyManager.phoneType),
            phoneCount = phoneCount,
            dataNetworkType = dataNetworkType,
        )
    }

    @Suppress("MissingPermission")
    private fun gatherSimSlots(
        application: Application,
        telephonyManager: TelephonyManager,
        subscriptionManager: SubscriptionManager?,
        phoneCount: Int,
        hasPhoneStatePermission: Boolean,
    ): List<SimSlotInfo> {
        if (hasPhoneStatePermission && subscriptionManager != null) {
            try {
                val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                if (activeSubscriptions != null && activeSubscriptions.isNotEmpty()) {
                    return activeSubscriptions.map { subInfo ->
                        val slotTm = telephonyManager.createForSubscriptionId(subInfo.subscriptionId)
                        SimSlotInfo(
                            slotIndex = subInfo.simSlotIndex,
                            status = mapSimState(slotTm.simState),
                            operatorName = subInfo.carrierName?.toString()
                                ?: slotTm.simOperatorName.ifEmpty { "Unknown" },
                            countryIso = subInfo.countryIso?.uppercase()
                                ?: slotTm.simCountryIso.uppercase().ifEmpty { "Unknown" },
                            networkType = mapNetworkType(slotTm.dataNetworkType),
                        )
                    }
                }
            } catch (_: SecurityException) {
                // Fall through to basic approach
            }
        }

        // Fallback: basic info from default TelephonyManager
        return (0 until phoneCount).map { slotIndex ->
            val simState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.getSimState(slotIndex)
            } else {
                if (slotIndex == 0) telephonyManager.simState else TelephonyManager.SIM_STATE_UNKNOWN
            }

            SimSlotInfo(
                slotIndex = slotIndex,
                status = mapSimState(simState),
                operatorName = if (slotIndex == 0) {
                    telephonyManager.simOperatorName.ifEmpty { "Unknown" }
                } else {
                    "Unknown"
                },
                countryIso = if (slotIndex == 0) {
                    telephonyManager.simCountryIso?.uppercase()?.ifEmpty { "Unknown" } ?: "Unknown"
                } else {
                    "Unknown"
                },
                networkType = if (slotIndex == 0 && hasPhoneStatePermission) {
                    mapNetworkType(telephonyManager.dataNetworkType)
                } else {
                    "Unknown"
                },
            )
        }
    }

    private fun mapSimState(state: Int): String = when (state) {
        TelephonyManager.SIM_STATE_READY -> "Present"
        TelephonyManager.SIM_STATE_ABSENT -> "Absent"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK required"
        TelephonyManager.SIM_STATE_NOT_READY -> "Not ready"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently disabled"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card I/O error"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Card restricted"
        else -> "Unknown"
    }

    @Suppress("MissingPermission")
    private fun mapNetworkType(type: Int): String = when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN,
        TelephonyManager.NETWORK_TYPE_GSM,
        -> "2G"

        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA,
        -> "3G"

        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN,
        -> "4G"

        TelephonyManager.NETWORK_TYPE_NR -> "5G"

        TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown"
        else -> "Unknown"
    }

    private fun mapPhoneType(type: Int): String = when (type) {
        TelephonyManager.PHONE_TYPE_GSM -> "GSM"
        TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
        TelephonyManager.PHONE_TYPE_SIP -> "SIP"
        TelephonyManager.PHONE_TYPE_NONE -> "None"
        else -> "Unknown"
    }
}
