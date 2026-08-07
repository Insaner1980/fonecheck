package com.insaner.fonecheck.ui.screens.simtelephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

fun interface SimTelephonyProvider {
    fun capture(): SimTelephonyInfo
}

class AndroidSimTelephonyProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SimTelephonyProvider {
        override fun capture(): SimTelephonyInfo {
            val hasHardware = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
            val hasPermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                    PackageManager.PERMISSION_GRANTED
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            if (!hasHardware || telephonyManager == null) {
                return SimTelephonyProbe.fromSnapshot(
                    SimTelephonySnapshot(
                        hasTelephonyHardware = hasHardware,
                        phoneStatePermissionGranted = hasPermission,
                        phoneCount = 0,
                        phoneTypeCode = TelephonyManager.PHONE_TYPE_NONE,
                        dataNetworkTypeCode = null,
                        slots = emptyList(),
                        sdkInt = Build.VERSION.SDK_INT,
                    ),
                )
            }

            val phoneCount =
                SimTelephonyProbe.modemCount(
                    sdkInt = Build.VERSION.SDK_INT,
                    activeModemCount = { telephonyManager.activeModemCount },
                    legacyPhoneCount = {
                        @Suppress("DEPRECATION")
                        telephonyManager.phoneCount
                    },
                )
            val subscriptions = activeSubscriptions(hasPermission).associateBy(SubscriptionInfo::getSimSlotIndex)
            val slots =
                (0 until phoneCount).map { slotIndex ->
                    val subscription = subscriptions[slotIndex]
                    val subscriptionTelephonyManager =
                        subscription?.let {
                            runCatching {
                                telephonyManager.createForSubscriptionId(it.subscriptionId)
                            }.getOrNull()
                        }
                    SimSlotSnapshot(
                        slotIndex = slotIndex,
                        stateCode =
                            runCatching { telephonyManager.getSimState(slotIndex) }
                                .getOrDefault(TelephonyManager.SIM_STATE_UNKNOWN),
                        activeSubscription = if (hasPermission) subscription != null else null,
                        embedded =
                            subscription?.let {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.isEmbedded else null
                            },
                        operatorName = subscription?.carrierName?.toString(),
                        countryIso = subscription?.countryIso,
                        networkTypeCode =
                            if (hasPermission) {
                                subscriptionTelephonyManager?.readDataNetworkType()
                            } else {
                                null
                            },
                    )
                }

            return SimTelephonyProbe.fromSnapshot(
                SimTelephonySnapshot(
                    hasTelephonyHardware = true,
                    phoneStatePermissionGranted = hasPermission,
                    phoneCount = phoneCount,
                    phoneTypeCode = runCatching { telephonyManager.phoneType }.getOrDefault(-1),
                    dataNetworkTypeCode = if (hasPermission) telephonyManager.readDataNetworkType() else null,
                    slots = slots,
                    sdkInt = Build.VERSION.SDK_INT,
                ),
            )
        }

        @Suppress("MissingPermission")
        private fun activeSubscriptions(hasPermission: Boolean): List<SubscriptionInfo> {
            if (!hasPermission) return emptyList()
            val manager = context.getSystemService(SubscriptionManager::class.java) ?: return emptyList()
            return try {
                manager.activeSubscriptionInfoList.orEmpty()
            } catch (_: SecurityException) {
                emptyList()
            } catch (_: RuntimeException) {
                emptyList()
            }
        }

        @Suppress("MissingPermission")
        private fun TelephonyManager.readDataNetworkType(): Int? =
            try {
                dataNetworkType
            } catch (_: SecurityException) {
                null
            } catch (_: RuntimeException) {
                null
            }
    }
