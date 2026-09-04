package com.insaner.fonecheck.domain.permission

enum class AppPermission {
    MICROPHONE,
    CAMERA,
    COARSE_LOCATION,
    FINE_LOCATION,
    PHONE,
    BLUETOOTH_CONNECT,
    ACTIVITY_RECOGNITION,
}

enum class PermissionKind {
    MICROPHONE,
    CAMERA,
    LOCATION,
    PHONE,
    BLUETOOTH,
    ACTIVITY_RECOGNITION,
}

enum class PermissionState {
    NOT_REQUESTED,
    GRANTED,
    DENIED,
    SETTINGS_RECOVERY,
    NOT_REQUIRED,
    HARDWARE_ABSENT,
    PARTIAL,
}

data class PermissionEvaluation(
    val sdkInt: Int,
    val hardwareAvailable: Boolean = true,
    val hasRequested: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val grantedPermissions: Set<AppPermission> = emptySet(),
)

object PermissionPolicy {
    fun evaluate(
        kind: PermissionKind,
        input: PermissionEvaluation,
    ): PermissionState {
        if (!input.hardwareAvailable) return PermissionState.HARDWARE_ABSENT

        val required = requiredPermissions(kind, input.sdkInt)
        if (required.isEmpty()) return PermissionState.NOT_REQUIRED

        val granted = required.intersect(input.grantedPermissions)
        return when {
            granted == required -> PermissionState.GRANTED
            granted.isNotEmpty() -> PermissionState.PARTIAL
            !input.hasRequested -> PermissionState.NOT_REQUESTED
            input.shouldShowRationale -> PermissionState.DENIED
            else -> PermissionState.SETTINGS_RECOVERY
        }
    }

    fun requiredPermissions(
        kind: PermissionKind,
        sdkInt: Int,
    ): Set<AppPermission> =
        when (kind) {
            PermissionKind.MICROPHONE -> setOf(AppPermission.MICROPHONE)
            PermissionKind.CAMERA -> setOf(AppPermission.CAMERA)
            PermissionKind.LOCATION -> setOf(AppPermission.COARSE_LOCATION, AppPermission.FINE_LOCATION)
            PermissionKind.PHONE -> setOf(AppPermission.PHONE)
            PermissionKind.BLUETOOTH ->
                if (sdkInt >= ANDROID_12_API_LEVEL) {
                    setOf(AppPermission.BLUETOOTH_CONNECT)
                } else {
                    emptySet()
                }
            PermissionKind.ACTIVITY_RECOGNITION ->
                if (sdkInt >= ANDROID_10_API_LEVEL) {
                    setOf(AppPermission.ACTIVITY_RECOGNITION)
                } else {
                    emptySet()
                }
        }

    private const val ANDROID_10_API_LEVEL = 29
    private const val ANDROID_12_API_LEVEL = 31
}
