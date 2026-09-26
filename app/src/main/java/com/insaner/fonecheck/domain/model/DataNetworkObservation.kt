package com.insaner.fonecheck.domain.model

import java.time.Instant

enum class NetworkReadState {
    RECEIVED,
    PENDING,
    UNSUPPORTED,
    NO_HARDWARE,
    NO_SUBSCRIPTION,
    PERMISSION_DENIED,
    FAILED,
    TIMED_OUT,
    STALE,
    UNRECOGNIZED,
}

data class NetworkDisplayObservation(
    val state: NetworkReadState,
    val baseType: Int? = null,
    val overrideType: Int? = null,
    // Receipt time, not the time the modem changed state.
    val receivedAt: Instant? = null,
)

data class DataNetworkObservation(
    val baseType: Int?,
    val baseReadAt: Instant?,
    val baseState: NetworkReadState,
    val display: NetworkDisplayObservation,
    val completedAt: Instant,
)

/** Public Android network constants. Unknown future values remain uninterpreted. */
fun baseNetworkName(type: Int?): String? =
    when (type) {
        1 -> "GPRS"
        2 -> "EDGE"
        3 -> "UMTS"
        4 -> "CDMA"
        5 -> "EVDO Rev.0"
        6 -> "EVDO Rev.A"
        7 -> "1xRTT"
        8 -> "HSDPA"
        9 -> "HSUPA"
        10 -> "HSPA"
        11 -> "iDen"
        12 -> "EVDO Rev.B"
        13 -> "LTE"
        14 -> "eHRPD"
        15 -> "HSPA+"
        16 -> "GSM"
        17 -> "TD-SCDMA"
        18 -> "IWLAN"
        19 -> "LTE CA"
        20 -> "NR"
        else -> null
    }

/** Generic indication only: never infer NSA/SA from NR_ADVANCED or carrier branding from LTE. */
fun NetworkDisplayObservation.indication(): String? {
    if (state != NetworkReadState.RECEIVED) return null
    return when (overrideType) {
        0 -> baseNetworkName(baseType)
        1 -> if (baseType == 13) "LTE CA" else null
        2 -> if (baseType == 13) "LTE Advanced Pro" else null
        3, 4 -> if (baseType == 13) "5G" else null
        5 -> if (baseType == 13 || baseType == 20) "5G" else null
        else -> null
    }
}

// Supplementary metadata must not create additional health/coverage checks.
val DiagnosticEvidence.isNetworkMetadata: Boolean
    get() = checkId.value in NETWORK_METADATA_IDS

private const val BASE_NETWORK_ID = "sim.base_network"
private const val DISPLAY_BASE_NETWORK_ID = "sim.display_base_network"

private val NETWORK_METADATA_IDS =
    setOf(BASE_NETWORK_ID, "sim.network_display", DISPLAY_BASE_NETWORK_ID, "sim.display_override")

/** Keep raw fields in JSON/comparison, and show the two scoped observations in ordinary UI/PDF. */
fun List<DiagnosticEvidence>.networkPresentation(): List<DiagnosticEvidence> {
    val hasBase = any { it.checkId.value == BASE_NETWORK_ID }
    return filterNot {
        it.checkId.value == DISPLAY_BASE_NETWORK_ID || it.checkId.value == "sim.display_override" ||
            (hasBase && it.checkId.value == "sim.network")
    }
}

fun DiagnosticEvidence.networkValueText(): String? =
    if (checkId.value == BASE_NETWORK_ID || checkId.value == DISPLAY_BASE_NETWORK_ID) {
        baseNetworkName((value as? EvidenceValue.IntValue)?.value)
    } else {
        null
    }

fun NetworkReadState.networkReason(): EvidenceReasonCode =
    EvidenceReasonCode(
        when (this) {
            NetworkReadState.RECEIVED -> "network_display_indication"
            NetworkReadState.PENDING -> "measurement_in_progress"
            NetworkReadState.UNSUPPORTED -> "android_version_unsupported"
            NetworkReadState.NO_HARDWARE -> "hardware_unavailable"
            NetworkReadState.NO_SUBSCRIPTION -> "network_no_subscription"
            NetworkReadState.PERMISSION_DENIED -> "permission_denied"
            NetworkReadState.FAILED -> "measurement_error"
            NetworkReadState.TIMED_OUT -> "measurement_timeout"
            NetworkReadState.STALE -> "network_observations_differ"
            NetworkReadState.UNRECOGNIZED -> "network_display_unknown"
        },
    )

fun DataNetworkObservation.toNetworkEvidence(): List<DiagnosticEvidence> {
    fun metadata(
        id: String,
        value: EvidenceValue?,
        time: Instant,
        reason: EvidenceReasonCode?,
        confidence: Confidence,
    ) = DiagnosticEvidence(
        categoryId = DiagnosticCategoryId.SIM,
        checkId = DiagnosticCheckId(DiagnosticCategoryId.SIM, "sim.$id"),
        status = DiagnosticStatus.INFO,
        confidence = confidence,
        source = EvidenceSource.ANDROID_API,
        applicability = Applicability.APPLICABLE,
        value = value,
        reason = reason,
        capturedAt = time,
    )

    val displayText = display.indication()
    return buildList {
        add(
            metadata(
                "base_network",
                baseType?.let { EvidenceValue.IntValue(it) },
                baseReadAt ?: completedAt,
                if (baseState == NetworkReadState.RECEIVED) {
                    EvidenceReasonCode("network_base_only")
                } else {
                    baseState.networkReason()
                },
                if (baseState == NetworkReadState.RECEIVED && baseNetworkName(baseType) != null) {
                    Confidence.HIGH
                } else {
                    Confidence.UNAVAILABLE
                },
            ),
        )
        add(
            metadata(
                "network_display",
                displayText?.let { EvidenceValue.RawTextValue(it) },
                display.receivedAt ?: completedAt,
                if (display.state == NetworkReadState.RECEIVED && displayText == null) {
                    NetworkReadState.UNRECOGNIZED.networkReason()
                } else {
                    display.state.networkReason()
                },
                if (displayText != null) Confidence.HIGH else Confidence.UNAVAILABLE,
            ),
        )
        display.receivedAt?.let { receivedAt ->
            display.baseType?.let {
                add(metadata("display_base_network", EvidenceValue.IntValue(it), receivedAt, null, Confidence.HIGH))
            }
            display.overrideType?.let {
                add(metadata("display_override", EvidenceValue.IntValue(it), receivedAt, null, Confidence.HIGH))
            }
        }
    }
}
