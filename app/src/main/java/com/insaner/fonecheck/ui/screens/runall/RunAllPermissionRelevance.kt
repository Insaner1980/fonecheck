package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.permission.PermissionKind

// Review membership and display order depend only on the selected run content, not permission state.
internal fun relevantRunAllPermissionKinds(
    targetCategory: DiagnosticCategoryId?,
    selections: RunAllSelections,
): List<PermissionKind> =
    buildList {
        if (targetCategory.includes(DiagnosticCategoryId.AUDIO) && selections.includeMicrophone) {
            add(PermissionKind.MICROPHONE)
        }
        if (targetCategory.includes(DiagnosticCategoryId.CAMERA) && selections.includeCamera) {
            add(PermissionKind.CAMERA)
        }
        if (targetCategory.includes(DiagnosticCategoryId.CONNECTIVITY)) {
            add(PermissionKind.LOCATION)
        }
        if (targetCategory.includes(DiagnosticCategoryId.SIM)) {
            add(PermissionKind.PHONE)
        }
        if (targetCategory.includes(DiagnosticCategoryId.CONNECTIVITY)) {
            add(PermissionKind.BLUETOOTH)
        }
    }

private fun DiagnosticCategoryId?.includes(category: DiagnosticCategoryId): Boolean = this == null || this == category

internal fun shouldAutoResolveRunAllPermissions(
    targetCategory: DiagnosticCategoryId?,
    relevantKinds: List<PermissionKind>,
): Boolean = targetCategory != null && relevantKinds.isEmpty()
