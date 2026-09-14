package com.insaner.fonecheck.ui.screens.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.thermalStatusCodeFromAndroid
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidThermalStatusReader
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : ThermalStatusReader {
        private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

        override fun read(): ThermalStatusCode {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalStatusCode.UNAVAILABLE
            val status =
                try {
                    powerManager?.currentThermalStatus
                } catch (_: RuntimeException) {
                    null
                }
            return status?.let(::thermalStatusCodeFromAndroid) ?: ThermalStatusCode.UNAVAILABLE
        }
    }
