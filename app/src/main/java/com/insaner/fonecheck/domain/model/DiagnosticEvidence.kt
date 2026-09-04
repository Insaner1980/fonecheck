package com.insaner.fonecheck.domain.model

import java.math.BigDecimal
import java.time.Instant

data class DiagnosticCheckId(
    val categoryId: DiagnosticCategoryId,
    val value: String,
) {
    init {
        require(value.matches(CHECK_ID_PATTERN)) { "Check ID must be a stable lower-case dotted identifier." }
        require(value.startsWith("${categoryId.stableId}.")) { "Check ID must start with its category stable ID." }
    }

    private companion object {
        val CHECK_ID_PATTERN = Regex("^[a-z][a-z0-9_]*(?:\\.[a-z][a-z0-9_]*)+$")
    }
}

enum class DiagnosticStatus {
    PASS,
    FAIL,
    WARNING,
    INFO,
    NOT_AVAILABLE,
    NOT_TESTED,
}

enum class EvidenceSource {
    AUTOMATIC_MEASUREMENT,
    ANDROID_API,
    USER_CONFIRMATION,
    DERIVED,
    ESTIMATE,
}

enum class Applicability {
    APPLICABLE,
    NOT_APPLICABLE,
}

@JvmInline
value class EvidenceReasonCode(
    val value: String,
) {
    init {
        require(value.matches(STABLE_CODE_PATTERN)) { "Reason code must be a stable lower-case snake-case code." }
    }

    companion object {
        val PERMISSION_DENIED = EvidenceReasonCode("permission_denied")
        val NOT_RUN = EvidenceReasonCode("not_run")
        val SKIPPED = EvidenceReasonCode("skipped")
        val CANCELLED = EvidenceReasonCode("cancelled")
        val TIMEOUT = EvidenceReasonCode("timeout")
        val INSUFFICIENT_SPACE = EvidenceReasonCode("insufficient_space")
        val ERROR = EvidenceReasonCode("error")
        val HARDWARE_UNAVAILABLE = EvidenceReasonCode("hardware_unavailable")
        val ANDROID_VERSION_UNSUPPORTED = EvidenceReasonCode("android_version_unsupported")
        val PLATFORM_RESTRICTION = EvidenceReasonCode("platform_restriction")
        val BIOMETRIC_LOCKOUT = EvidenceReasonCode("biometric_lockout")
        val BIOMETRIC_NOT_ENROLLED = EvidenceReasonCode("biometric_not_enrolled")
        val DISABLED = EvidenceReasonCode("disabled")
        val USER_CONFIRMED_FAILURE = EvidenceReasonCode("user_confirmed_failure")
        val DEGRADED = EvidenceReasonCode("degraded")
    }
}

@JvmInline
value class EvidenceUnitCode(
    val value: String,
) {
    init {
        require(value.matches(STABLE_CODE_PATTERN)) { "Unit code must be a stable lower-case code." }
    }
}

sealed interface EvidenceValue {
    data class BooleanValue(
        val value: Boolean,
    ) : EvidenceValue

    data class IntValue(
        val value: Int,
    ) : EvidenceValue

    data class LongValue(
        val value: Long,
    ) : EvidenceValue

    data class DecimalValue(
        val value: BigDecimal,
    ) : EvidenceValue

    data class DoubleValue(
        val value: Double,
    ) : EvidenceValue {
        init {
            require(value.isFinite()) { "Double evidence must be finite." }
        }
    }

    data class RawTextValue(
        val value: String,
    ) : EvidenceValue

    data class StableTextCodeValue(
        val value: String,
    ) : EvidenceValue {
        init {
            require(value.matches(STABLE_CODE_PATTERN)) { "Stable text code must be a lower-case snake-case code." }
        }
    }
}

data class DiagnosticEvidence(
    val categoryId: DiagnosticCategoryId,
    val checkId: DiagnosticCheckId,
    val status: DiagnosticStatus,
    val confidence: Confidence,
    val source: EvidenceSource,
    val applicability: Applicability,
    val reason: EvidenceReasonCode? = null,
    val value: EvidenceValue? = null,
    val unit: EvidenceUnitCode? = null,
    val capturedAt: Instant,
) {
    init {
        require(checkId.categoryId == categoryId) { "Check ID category must match evidence category." }
    }
}

private val STABLE_CODE_PATTERN = Regex("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$")
