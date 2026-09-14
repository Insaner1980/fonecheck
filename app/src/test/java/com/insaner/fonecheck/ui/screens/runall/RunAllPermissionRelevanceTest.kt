package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.permission.PermissionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunAllPermissionRelevanceTest {
    @Test
    fun fullCheckPreservesSelectionSensitiveDisplayOrder() {
        val required = listOf(PermissionKind.LOCATION, PermissionKind.PHONE, PermissionKind.BLUETOOTH)
        val cases =
            listOf(
                RunAllSelections() to listOf(PermissionKind.MICROPHONE, PermissionKind.CAMERA) + required,
                RunAllSelections(includeMicrophone = false) to listOf(PermissionKind.CAMERA) + required,
                RunAllSelections(includeCamera = false) to listOf(PermissionKind.MICROPHONE) + required,
                RunAllSelections(includeMicrophone = false, includeCamera = false) to required,
            )
        cases.forEach { (selections, expected) ->
            assertEquals(expected, relevantRunAllPermissionKinds(null, selections))
            assertEquals(
                expected,
                relevantRunAllPermissionKinds(
                    null,
                    selections.copy(includeSpeaker = false, includeStorageBenchmark = false),
                ),
            )
        }
    }

    @Test
    fun everyCategoryHasOnlyItsFullCheckPermissionsInDisplayOrder() {
        val expected =
            mapOf(
                DiagnosticCategoryId.DEVICE to emptyList(),
                DiagnosticCategoryId.PERFORMANCE to emptyList(),
                DiagnosticCategoryId.SIM to listOf(PermissionKind.PHONE),
                DiagnosticCategoryId.DISPLAY to emptyList(),
                DiagnosticCategoryId.AUDIO to listOf(PermissionKind.MICROPHONE),
                DiagnosticCategoryId.CAMERA to listOf(PermissionKind.CAMERA),
                DiagnosticCategoryId.SENSORS to emptyList(),
                DiagnosticCategoryId.CONNECTIVITY to listOf(PermissionKind.LOCATION, PermissionKind.BLUETOOTH),
                DiagnosticCategoryId.BATTERY to emptyList(),
                DiagnosticCategoryId.THERMAL to emptyList(),
                DiagnosticCategoryId.STORAGE to emptyList(),
                DiagnosticCategoryId.VIBRATION to emptyList(),
                DiagnosticCategoryId.BUTTONS to emptyList(),
                DiagnosticCategoryId.BIOMETRICS to emptyList(),
            )
        assertEquals(DiagnosticCategoryId.entries.toSet(), expected.keys)
        expected.forEach { (category, kinds) ->
            val actual = relevantRunAllPermissionKinds(category, RunAllSelections())
            assertEquals(category.name, kinds, actual)
            assertEquals(category.name, kinds.isEmpty(), shouldAutoResolveRunAllPermissions(category, actual))
        }
    }

    @Test
    fun deselectedMicrophoneAndCameraLeaveTheirCategoryReviewEmpty() {
        val cases =
            listOf(
                DiagnosticCategoryId.AUDIO to RunAllSelections(includeMicrophone = false),
                DiagnosticCategoryId.CAMERA to RunAllSelections(includeCamera = false),
            )
        cases.forEach { (category, selections) ->
            val kinds = relevantRunAllPermissionKinds(category, selections)
            assertEquals(emptyList<PermissionKind>(), kinds)
            assertTrue(shouldAutoResolveRunAllPermissions(category, kinds))
        }
    }

    @Test
    fun fullCheckNeverAutoResolvesEvenWithAnEmptyRelevanceList() {
        assertFalse(shouldAutoResolveRunAllPermissions(null, emptyList()))
        assertFalse(shouldAutoResolveRunAllPermissions(null, relevantRunAllPermissionKinds(null, RunAllSelections())))
    }
}
