package com.insaner.fonecheck.domain.model

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
    val isRooted: Boolean,
    val developerOptionsEnabled: Boolean,
    val usbDebuggingEnabled: Boolean,
) {
    companion object {
        const val UNAVAILABLE = "unavailable"
    }
}
