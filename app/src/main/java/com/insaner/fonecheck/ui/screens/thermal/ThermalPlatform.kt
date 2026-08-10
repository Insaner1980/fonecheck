package com.insaner.fonecheck.ui.screens.thermal

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.insaner.fonecheck.domain.model.ThermalStatusCode

fun interface ThermalStatusRegistration {
    fun close()
}

interface ThermalPlatform {
    val statusApiSupported: Boolean
    val headroomApiSupported: Boolean

    fun readStatus(): ThermalStatusCode?

    fun readHeadroom(): Float?

    fun readBatteryTemperatureCelsius(): Float?

    fun registerStatusListener(listener: (ThermalStatusCode) -> Unit): ThermalStatusRegistration?
}

class AndroidThermalPlatform(
    private val context: Context,
) : ThermalPlatform {
    private val powerManager = context.getSystemService(PowerManager::class.java)

    override val statusApiSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null

    override val headroomApiSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && powerManager != null

    override fun readStatus(): ThermalStatusCode? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || powerManager == null) return null
        return readStatusApi29()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun readStatusApi29(): ThermalStatusCode? =
        try {
            ThermalRuntimePolicy.status(Build.VERSION.SDK_INT, powerManager?.currentThermalStatus)
        } catch (_: RuntimeException) {
            null
        }

    override fun readHeadroom(): Float? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || powerManager == null) return null
        return readHeadroomApi30()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun readHeadroomApi30(): Float? =
        try {
            ThermalRuntimePolicy.headroom(
                sdkInt = Build.VERSION.SDK_INT,
                rawHeadroom = powerManager?.getThermalHeadroom(HEADROOM_FORECAST_SECONDS),
            )
        } catch (_: RuntimeException) {
            null
        }

    override fun readBatteryTemperatureCelsius(): Float? {
        val intent =
            try {
                ContextCompat.registerReceiver(
                    context,
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    ContextCompat.RECEIVER_EXPORTED,
                )
            } catch (_: RuntimeException) {
                null
            }
        val rawTemperature =
            intent
                ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                ?.takeIf { intent.hasExtra(BatteryManager.EXTRA_TEMPERATURE) }
        return ThermalRuntimePolicy.batteryTemperature(rawTemperature)
    }

    override fun registerStatusListener(listener: (ThermalStatusCode) -> Unit): ThermalStatusRegistration? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || powerManager == null) return null
        return registerStatusListenerApi29(listener)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun registerStatusListenerApi29(listener: (ThermalStatusCode) -> Unit): ThermalStatusRegistration? {
        val manager = powerManager ?: return null
        val platformListener =
            PowerManager.OnThermalStatusChangedListener { rawStatus ->
                listener(ThermalRuntimePolicy.status(Build.VERSION.SDK_INT, rawStatus))
            }
        try {
            manager.addThermalStatusListener(context.mainExecutor, platformListener)
        } catch (_: RuntimeException) {
            return null
        }
        var closed = false
        return ThermalStatusRegistration {
            if (!closed) {
                closed = true
                try {
                    manager.removeThermalStatusListener(platformListener)
                } catch (_: RuntimeException) {
                    // Listener removal is best-effort after the platform has released it.
                }
            }
        }
    }

    private companion object {
        const val HEADROOM_FORECAST_SECONDS = 0
    }
}
