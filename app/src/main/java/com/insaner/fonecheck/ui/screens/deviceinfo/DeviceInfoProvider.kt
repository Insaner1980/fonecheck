package com.insaner.fonecheck.ui.screens.deviceinfo

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.runtime.EpochMillisClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

fun interface DeviceInfoProvider {
    fun capture(): DeviceInfo
}

class AndroidDeviceInfoProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clock: EpochMillisClock,
    ) : DeviceInfoProvider {
        override fun capture(): DeviceInfo =
            DeviceInfo(
                model = DeviceValueNormalizer.text(Build.MODEL),
                manufacturer = DeviceValueNormalizer.text(Build.MANUFACTURER),
                brand = DeviceValueNormalizer.text(Build.BRAND),
                product = DeviceValueNormalizer.text(Build.PRODUCT),
                androidVersion = DeviceValueNormalizer.text(Build.VERSION.RELEASE),
                apiLevel = Build.VERSION.SDK_INT,
                securityPatch = DeviceValueNormalizer.securityPatch(Build.VERSION.SECURITY_PATCH),
                buildNumber = DeviceValueNormalizer.text(Build.DISPLAY),
                kernelVersion = DeviceValueNormalizer.text(System.getProperty("os.version")),
                basebandVersion = DeviceValueNormalizer.text(readRadioVersion()),
                bootloaderVersion = DeviceValueNormalizer.text(Build.BOOTLOADER),
                widevineLevel = readWidevineLevel(),
                rootArtifactDetected = RootArtifactHeuristic.detect { File(it).exists() },
                developerOptionsEnabled = readGlobalSetting(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED),
                usbDebuggingEnabled = readGlobalSetting(Settings.Global.ADB_ENABLED),
                capturedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
            )

        private fun readWidevineLevel(): String =
            readDrmSecurityLevel {
                val mediaDrm = MediaDrm(WIDEVINE_UUID)
                object : DrmPropertySession {
                    override fun securityLevel(): String = mediaDrm.getPropertyString("securityLevel")

                    override fun close() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            mediaDrm.close()
                        } else {
                            @Suppress("DEPRECATION")
                            mediaDrm.release()
                        }
                    }
                }
            }

        private fun readRadioVersion(): String? =
            try {
                Build.getRadioVersion()
            } catch (_: RuntimeException) {
                null
            }

        private fun readGlobalSetting(name: String): Boolean =
            try {
                Settings.Global.getInt(context.contentResolver, name, 0) != 0
            } catch (_: SecurityException) {
                false
            }

        private companion object {
            val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        }
    }
