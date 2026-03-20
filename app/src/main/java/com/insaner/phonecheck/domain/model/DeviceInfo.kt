package com.insaner.phonecheck.domain.model

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val brand: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
)
