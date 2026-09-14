package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.permission.PermissionKind

// Review membership and display order depend only on the selected run content, not permission state.
internal fun relevantRunAllPermissionKinds(
    targetCategory: DiagnosticCategoryId?,
    selections: RunAllSelections,
): List<PermissionKind> =
    buildList {
        if ((targetCategory == null || targetCategory == DiagnosticCategoryId.AUDIO) && selections.includeMicrophone) {
            add(PermissionKind.MICROPHONE)
        }
        if ((targetCategory == null || targetCategory == DiagnosticCategoryId.CAMERA) && selections.includeCamera) {
            add(PermissionKind.CAMERA)
        }
        if (targetCategory == null || targetCategory == DiagnosticCategoryId.CONNECTIVITY) {
            add(PermissionKind.LOCATION)
        }
        if (targetCategory == null || targetCategory == DiagnosticCategoryId.SIM) {
            add(PermissionKind.PHONE)
        }
        if (targetCategory == null || targetCategory == DiagnosticCategoryId.CONNECTIVITY) {
            add(PermissionKind.BLUETOOTH)
        }
    }

internal fun shouldAutoResolveRunAllPermissions(
    targetCategory: DiagnosticCategoryId?,
    relevantKinds: List<PermissionKind>,
): Boolean = targetCategory != null && relevantKinds.isEmpty()
