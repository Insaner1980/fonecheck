package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.ui.permissions.PermissionController

@Composable
internal fun runAllPermissionPrompts(
    relevantKinds: List<PermissionKind>,
    controllers: List<PermissionController>,
    onRequest: (PermissionController) -> Unit,
): List<PermissionPrompt> =
    relevantKinds.map { kind ->
        val (title, rationale) =
            when (kind) {
                PermissionKind.MICROPHONE ->
                    R.string.settings_permission_microphone to R.string.permission_rationale_microphone
                PermissionKind.CAMERA ->
                    R.string.settings_permission_camera to R.string.permission_rationale_camera
                PermissionKind.LOCATION ->
                    R.string.settings_permission_location to R.string.permission_rationale_location
                PermissionKind.PHONE ->
                    R.string.settings_permission_phone to R.string.permission_rationale_phone
                PermissionKind.BLUETOOTH ->
                    R.string.settings_permission_bluetooth to R.string.permission_rationale_bluetooth
                PermissionKind.ACTIVITY_RECOGNITION ->
                    error("Activity recognition is not part of Full Check permission review")
            }
        val controller = controllers.single { it.kind == kind }
        PermissionPrompt(
            state = controller.state,
            title = stringResource(title),
            rationale = stringResource(rationale),
            onRequest = { onRequest(controller) },
            onOpenSettings = controller::openSettings,
        )
    }
