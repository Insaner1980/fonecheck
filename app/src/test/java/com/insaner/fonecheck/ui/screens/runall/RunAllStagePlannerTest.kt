package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunAllStagePlannerTest {
    @Test
    fun fullHardwareAndPermissionsProduceCatalogOrderedApplicablePlan() {
        val plan =
            RunAllStagePlanner.plan(
                hardware = fullHardware(),
                permissions = fullPermissions(),
                selections = RunAllSelections(),
            )

        assertEquals(DiagnosticCatalog.categories, plan.categories.map { it.categoryId })
        assertEquals(
            listOf(
                RunAllStage.AUTOMATIC,
                RunAllStage.DISPLAY,
                RunAllStage.AUDIO,
                RunAllStage.CAMERA,
                RunAllStage.SENSORS,
                RunAllStage.VIBRATION,
                RunAllStage.BUTTONS,
                RunAllStage.BIOMETRICS,
                RunAllStage.RESULTS,
            ),
            plan.stages,
        )
        assertEquals(7, plan.interactiveStages.size)
    }

    @Test
    fun absentHardwareAndDeniedCameraRemoveOnlyInapplicableInteractiveStages() {
        val plan =
            RunAllStagePlanner.plan(
                hardware =
                    fullHardware().copy(
                        cameraAvailable = false,
                        motionSensorAvailable = false,
                        vibratorAvailable = false,
                        biometricsAvailable = false,
                    ),
                permissions = fullPermissions().copy(camera = false, location = false),
                selections = RunAllSelections(),
            )

        assertFalse(RunAllStage.CAMERA in plan.stages)
        assertFalse(RunAllStage.SENSORS in plan.stages)
        assertFalse(RunAllStage.VIBRATION in plan.stages)
        assertFalse(RunAllStage.BIOMETRICS in plan.stages)
        assertTrue(RunAllStage.RESULTS in plan.stages)
        assertEquals(
            RunAllCategoryDisposition.NOT_APPLICABLE,
            plan.category(DiagnosticCategoryId.CAMERA).disposition,
        )
        assertEquals(
            RunAllCategoryDisposition.PERMISSION_LIMITED,
            plan.category(DiagnosticCategoryId.CONNECTIVITY).disposition,
        )
    }

    @Test
    fun userChoicesSkipWorkloadsWithoutRemovingAutomaticCategoryEvidence() {
        val plan =
            RunAllStagePlanner.plan(
                hardware = fullHardware(),
                permissions = fullPermissions(),
                selections =
                    RunAllSelections(
                        includeSpeaker = false,
                        includeMicrophone = false,
                        includeCamera = false,
                        includeStorageBenchmark = false,
                    ),
            )

        assertFalse(RunAllStage.AUDIO in plan.stages)
        assertFalse(RunAllStage.CAMERA in plan.stages)
        assertEquals(
            RunAllCategoryDisposition.AUTOMATIC,
            plan.category(DiagnosticCategoryId.AUDIO).disposition,
        )
        assertEquals(
            RunAllCategoryDisposition.SKIPPED_BY_USER,
            plan.category(DiagnosticCategoryId.CAMERA).disposition,
        )
        assertEquals(
            RunAllCategoryDisposition.AUTOMATIC,
            plan.category(DiagnosticCategoryId.STORAGE).disposition,
        )
    }

    @Test
    fun interactiveProgressUsesActualPlannedStages() {
        val plan =
            RunAllStagePlanner.plan(
                hardware = fullHardware().copy(vibratorAvailable = false),
                permissions = fullPermissions().copy(camera = false),
                selections = RunAllSelections(),
            )

        assertEquals(RunAllProgress(position = 2, total = 5), plan.progressFor(RunAllStage.AUDIO))
        assertEquals(RunAllProgress(position = 5, total = 5), plan.progressFor(RunAllStage.BIOMETRICS))
    }

    private fun fullHardware() =
        RunAllHardwareProfile(
            microphoneAvailable = true,
            cameraAvailable = true,
            motionSensorAvailable = true,
            vibratorAvailable = true,
            biometricsAvailable = true,
        )

    private fun fullPermissions() =
        RunAllPermissions(
            microphone = true,
            camera = true,
            location = true,
            phone = true,
            bluetooth = true,
        )
}
