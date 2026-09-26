package com.insaner.fonecheck.ui.screens.simtelephony

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.telephony.PhoneStateListener
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.insaner.fonecheck.domain.model.DataNetworkObservation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class AndroidDataNetworkProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        suspend fun capture(): DataNetworkObservation =
            // The API 30 PhoneStateListener owns a main Looper; registration and removal stay together.
            withContext(Dispatchers.Main.immediate) {
                DataNetworkCapture(AndroidPlatform(context), Instant::now, SystemClock::elapsedRealtime).capture()
            }
    }

@SuppressLint("MissingPermission") // Capture checks permission and handles revocation at every public API boundary.
private class AndroidPlatform(
    private val context: Context,
) : DataNetworkPlatform {
    private val manager = context.getSystemService(TelephonyManager::class.java)

    override val displaySupported: Boolean = Build.VERSION.SDK_INT >= 30

    override fun hasHardware(): Boolean =
        manager != null && context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    override fun dataSubscription(): Int? =
        (
            if (Build.VERSION.SDK_INT >= 30) {
                SubscriptionManager.getActiveDataSubscriptionId()
            } else {
                SubscriptionManager.getDefaultDataSubscriptionId()
            }
        ).takeIf { it >= 0 && it != Int.MAX_VALUE }

    override fun readBase(subscription: Int): Int =
        requireNotNull(manager).createForSubscriptionId(subscription).dataNetworkType

    override fun listen(
        subscription: Int,
        onDisplay: (Int, Int) -> Unit,
        onSubscriptionChanged: () -> Unit,
    ): () -> Unit {
        val bound = requireNotNull(manager).createForSubscriptionId(subscription)
        return if (Build.VERSION.SDK_INT >= 31) {
            listenModern(bound, subscription, onDisplay, onSubscriptionChanged)
        } else if (Build.VERSION.SDK_INT >= 30) {
            listenLegacy(bound, subscription, onDisplay, onSubscriptionChanged)
        } else {
            error("Display information is unavailable below API 30")
        }
    }

    @RequiresApi(31)
    @Suppress("TooGenericExceptionCaught") // Any registration failure must unwind a partially registered callback.
    private fun listenModern(
        bound: TelephonyManager,
        subscription: Int,
        onDisplay: (Int, Int) -> Unit,
        onSubscriptionChanged: () -> Unit,
    ): () -> Unit {
        val callback =
            object :
                TelephonyCallback(),
                TelephonyCallback.DisplayInfoListener,
                TelephonyCallback.ActiveDataSubscriptionIdListener {
                override fun onDisplayInfoChanged(info: TelephonyDisplayInfo) {
                    onDisplay(info.networkType, info.overrideNetworkType)
                }

                override fun onActiveDataSubscriptionIdChanged(subId: Int) {
                    if (subId != subscription) onSubscriptionChanged()
                }
            }
        val stop = { bound.unregisterTelephonyCallback(callback) }
        try {
            bound.registerTelephonyCallback(context.mainExecutor, callback)
        } catch (error: RuntimeException) {
            runCatching(stop)
            throw error
        }
        return stop
    }

    @RequiresApi(30)
    @Suppress("DEPRECATION", "TooGenericExceptionCaught") // Registration failure must unwind a partial listener.
    private fun listenLegacy(
        bound: TelephonyManager,
        subscription: Int,
        onDisplay: (Int, Int) -> Unit,
        onSubscriptionChanged: () -> Unit,
    ): () -> Unit {
        val listener =
            object : PhoneStateListener() {
                override fun onDisplayInfoChanged(info: TelephonyDisplayInfo) {
                    onDisplay(info.networkType, info.overrideNetworkType)
                }

                override fun onActiveDataSubscriptionIdChanged(subId: Int) {
                    if (subId != subscription) onSubscriptionChanged()
                }
            }
        val stop = { bound.listen(listener, PhoneStateListener.LISTEN_NONE) }
        try {
            bound.listen(
                listener,
                PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED or
                    PhoneStateListener.LISTEN_ACTIVE_DATA_SUBSCRIPTION_ID_CHANGE,
            )
        } catch (error: RuntimeException) {
            runCatching(stop)
            throw error
        }
        return stop
    }
}
