package com.insaner.fonecheck.domain.model

enum class TelephonyHardwareCode {
    AVAILABLE,
    NO_HARDWARE,
}

enum class SimInventoryCode {
    NO_TELEPHONY,
    NO_SIM,
    INACTIVE_SIM,
    SINGLE_SIM,
    MULTIPLE_SIM,
    UNKNOWN,
}

enum class SimSlotStateCode {
    READY,
    ABSENT,
    NETWORK_LOCKED,
    PIN_REQUIRED,
    PUK_REQUIRED,
    NOT_READY,
    PERMANENTLY_DISABLED,
    CARD_IO_ERROR,
    CARD_RESTRICTED,
    UNKNOWN,
}

enum class SimActivityCode {
    ACTIVE,
    INACTIVE,
    UNKNOWN,
}

enum class SimFormFactorCode {
    EMBEDDED,
    PHYSICAL,
    UNKNOWN,
}

enum class PhoneTypeCode {
    GSM,
    CDMA,
    SIP,
    NONE,
    UNKNOWN,
}

enum class NetworkGenerationCode {
    SECOND_GENERATION,
    THIRD_GENERATION,
    FOURTH_GENERATION,
    FIFTH_GENERATION,
    UNKNOWN,
}

data class SimSlotInfo(
    val slotIndex: Int,
    val state: SimSlotStateCode,
    val activity: SimActivityCode,
    val formFactor: SimFormFactorCode,
    val operatorName: String?,
    val countryIso: String?,
    val networkType: NetworkGenerationCode,
)

data class SimTelephonyInfo(
    val hardware: TelephonyHardwareCode,
    val inventory: SimInventoryCode,
    val simSlots: List<SimSlotInfo>,
    val phoneType: PhoneTypeCode,
    val phoneCount: Int,
    val dataNetworkType: NetworkGenerationCode,
    val phoneStatePermissionGranted: Boolean,
) {
    val isDualSim: Boolean
        get() = inventory == SimInventoryCode.MULTIPLE_SIM
}
