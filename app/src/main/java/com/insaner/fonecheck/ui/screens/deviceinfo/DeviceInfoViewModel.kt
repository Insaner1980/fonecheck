package com.insaner.fonecheck.ui.screens.deviceinfo

import android.app.Application
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import com.insaner.fonecheck.domain.model.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        val deviceInfo: DeviceInfo = gatherDeviceInfo(application)

        private fun gatherDeviceInfo(application: Application): DeviceInfo {
            val contentResolver = application.contentResolver
            return DeviceInfo(
                model = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                brand = Build.BRAND,
                product = Build.PRODUCT,
                androidVersion = Build.VERSION.RELEASE,
                apiLevel = Build.VERSION.SDK_INT,
                securityPatch = Build.VERSION.SECURITY_PATCH,
                buildNumber = Build.DISPLAY,
                kernelVersion = System.getProperty("os.version") ?: "Unknown",
                basebandVersion = Build.getRadioVersion() ?: "Unknown",
                bootloaderVersion = Build.BOOTLOADER,
                widevineLevel = getWidevineLevel(),
                isRooted = checkRootStatus(),
                developerOptionsEnabled =
                    Settings.Global.getInt(
                        contentResolver,
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                        0,
                    ) != 0,
                usbDebuggingEnabled =
                    Settings.Global.getInt(
                        contentResolver,
                        Settings.Global.ADB_ENABLED,
                        0,
                    ) != 0,
            )
        }

        private fun getWidevineLevel(): String =
            try {
                val widevineUuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
                val drm = MediaDrm(widevineUuid)
                val level = drm.getPropertyString("securityLevel")
                drm.close()
                level
            } catch (_: Exception) {
                "Not supported"
            }

        private fun checkRootStatus(): Boolean {
            val suPaths =
                listOf(
                    "/system/app/Superuser.apk",
                    "/system/xbin/su",
                    "/system/bin/su",
                    "/sbin/su",
                    "/data/local/xbin/su",
                    "/data/local/bin/su",
                )
            return suPaths.any { File(it).exists() }
        }
    }
