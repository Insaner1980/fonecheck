package com.insaner.fonecheck.domain.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionPolicyTest {
    @Test
    fun activityRecognitionIsRequiredOnlyFromApi29() {
        assertEquals(
            emptySet<AppPermission>(),
            PermissionPolicy.requiredPermissions(PermissionKind.ACTIVITY_RECOGNITION, 28),
        )
        assertEquals(
            setOf(AppPermission.ACTIVITY_RECOGNITION),
            PermissionPolicy.requiredPermissions(PermissionKind.ACTIVITY_RECOGNITION, 29),
        )
    }

    @Test
    fun bluetoothConnectIsNotRequiredBeforeApi31() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.BLUETOOTH,
                input = PermissionEvaluation(sdkInt = 30),
            )

        assertEquals(PermissionState.NOT_REQUIRED, state)
        assertEquals(emptySet<AppPermission>(), PermissionPolicy.requiredPermissions(PermissionKind.BLUETOOTH, 30))
    }

    @Test
    fun bluetoothConnectIsRequiredFromApi31() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.BLUETOOTH,
                input = PermissionEvaluation(sdkInt = 31),
            )

        assertEquals(PermissionState.NOT_REQUESTED, state)
        assertEquals(
            setOf(AppPermission.BLUETOOTH_CONNECT),
            PermissionPolicy.requiredPermissions(PermissionKind.BLUETOOTH, 31),
        )
    }

    @Test
    fun absentHardwareOverridesPermissionRequestState() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.CAMERA,
                input =
                    PermissionEvaluation(
                        sdkInt = 36,
                        hardwareAvailable = false,
                        hasRequested = true,
                    ),
            )

        assertEquals(PermissionState.HARDWARE_ABSENT, state)
    }

    @Test
    fun allRequiredPermissionsProduceGrantedState() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.MICROPHONE,
                input =
                    PermissionEvaluation(
                        sdkInt = 36,
                        grantedPermissions = setOf(AppPermission.MICROPHONE),
                    ),
            )

        assertEquals(PermissionState.GRANTED, state)
    }

    @Test
    fun rationaleAfterRequestProducesDeniedState() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.PHONE,
                input =
                    PermissionEvaluation(
                        sdkInt = 36,
                        hasRequested = true,
                        shouldShowRationale = true,
                    ),
            )

        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun denialWithoutRationaleAfterRequestProducesSettingsRecoveryState() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.PHONE,
                input =
                    PermissionEvaluation(
                        sdkInt = 36,
                        hasRequested = true,
                        shouldShowRationale = false,
                    ),
            )

        assertEquals(PermissionState.SETTINGS_RECOVERY, state)
    }

    @Test
    fun approximateLocationProducesPartialState() {
        val state =
            PermissionPolicy.evaluate(
                kind = PermissionKind.LOCATION,
                input =
                    PermissionEvaluation(
                        sdkInt = 36,
                        hasRequested = true,
                        grantedPermissions = setOf(AppPermission.COARSE_LOCATION),
                    ),
            )

        assertEquals(PermissionState.PARTIAL, state)
        assertEquals(
            setOf(AppPermission.COARSE_LOCATION, AppPermission.FINE_LOCATION),
            PermissionPolicy.requiredPermissions(PermissionKind.LOCATION, 36),
        )
    }
}
