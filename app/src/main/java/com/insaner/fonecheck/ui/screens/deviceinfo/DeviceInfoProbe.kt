package com.insaner.fonecheck.ui.screens.deviceinfo

import com.insaner.fonecheck.domain.model.DeviceInfo
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale

internal object DeviceValueNormalizer {
    fun text(value: String?): String {
        val normalized = value?.trim().orEmpty()
        return normalized.takeUnless {
            it.isEmpty() || it.lowercase(Locale.ROOT) in unavailableValues
        } ?: DeviceInfo.UNAVAILABLE
    }

    fun securityPatch(value: String?): String {
        val normalized = text(value)
        if (normalized == DeviceInfo.UNAVAILABLE) return normalized
        return try {
            LocalDate.parse(normalized).toString()
        } catch (_: DateTimeParseException) {
            DeviceInfo.UNAVAILABLE
        }
    }

    private val unavailableValues = setOf("unknown", "n/a", "not supported", "null")
}

internal object RootArtifactHeuristic {
    val knownPaths =
        listOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
        )

    fun detect(exists: (String) -> Boolean): Boolean =
        knownPaths.any { path ->
            try {
                exists(path)
            } catch (_: SecurityException) {
                false
            }
        }
}

internal interface DrmPropertySession : AutoCloseable {
    fun securityLevel(): String
}

internal fun readDrmSecurityLevel(openSession: () -> DrmPropertySession): String {
    val session =
        try {
            openSession()
        } catch (_: Exception) {
            return DeviceInfo.UNAVAILABLE
        }

    return try {
        DeviceValueNormalizer.text(session.securityLevel())
    } catch (_: Exception) {
        DeviceInfo.UNAVAILABLE
    } finally {
        try {
            session.close()
        } catch (_: Exception) {
            // The diagnostic value is still valid even if the platform cleanup reports an error.
        }
    }
}
