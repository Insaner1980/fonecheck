package com.insaner.phonecheck.domain.model

data class SimSlotInfo(
    val slotIndex: Int,
    val status: String,
    val operatorName: String,
    val countryIso: String,
    val networkType: String,
)

data class SimTelephonyInfo(
    val simSlots: List<SimSlotInfo>,
    val isDualSim: Boolean,
    val phoneType: String,
    val phoneCount: Int,
    val dataNetworkType: String,
)
