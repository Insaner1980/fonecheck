package com.insaner.fonecheck.domain.model

import java.time.Instant

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val brand: String,
    val product: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val buildNumber: String,
    val kernelVersion: String,
    val basebandVersion: String,
    val bootloaderVersion: String,
    val widevineLevel: String,
    val rootArtifactDetected: Boolean,
    val developerOptionsEnabled: Boolean,
    val usbDebuggingEnabled: Boolean,
    val capturedAt: Instant,
) {
    companion object {
        const val UNAVAILABLE = "unavailable"
    }
}
