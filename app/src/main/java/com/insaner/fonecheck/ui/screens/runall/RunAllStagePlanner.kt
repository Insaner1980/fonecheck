package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId

data class RunAllSelections(
    val includeSpeaker: Boolean = true,
    val includeMicrophone: Boolean = true,
    val includeCamera: Boolean = true,
    val includeStorageBenchmark: Boolean = true,
)

data class RunAllHardwareProfile(
    val microphoneAvailable: Boolean = false,
    val cameraAvailable: Boolean = false,
    val motionSensorAvailable: Boolean = false,
    val vibratorAvailable: Boolean = false,
    val biometricsAvailable: Boolean = false,
) {
    companion object {
        val ALL_AVAILABLE =
            RunAllHardwareProfile(
                microphoneAvailable = true,
                cameraAvailable = true,
                motionSensorAvailable = true,
                vibratorAvailable = true,
                biometricsAvailable = true,
            )
    }
}

enum class RunAllCategoryDisposition {
    AUTOMATIC,
    INTERACTIVE,
    PERMISSION_LIMITED,
    SKIPPED_BY_USER,
    NOT_APPLICABLE,
}

data class RunAllCategoryPlan(
    val categoryId: DiagnosticCategoryId,
    val stage: RunAllStage?,
    val disposition: RunAllCategoryDisposition,
)

data class RunAllProgress(
    val position: Int,
    val total: Int,
)

data class RunAllPlan(
    val categories: List<RunAllCategoryPlan>,
    val stages: List<RunAllStage>,
) {
    val interactiveStages: List<RunAllStage> =
        stages.filterNot { it == RunAllStage.AUTOMATIC || it == RunAllStage.RESULTS }

    fun category(categoryId: DiagnosticCategoryId): RunAllCategoryPlan =
        categories.first { it.categoryId == categoryId }

    fun progressFor(stage: RunAllStage): RunAllProgress {
        val index = interactiveStages.indexOf(stage)
        require(index >= 0) { "Stage $stage is not interactive in this plan." }
        return RunAllProgress(position = index + 1, total = interactiveStages.size)
    }

    companion object {
        val EMPTY = RunAllPlan(categories = emptyList(), stages = emptyList())
    }
}

object RunAllStagePlanner {
    fun plan(
        hardware: RunAllHardwareProfile,
        permissions: RunAllPermissions,
        selections: RunAllSelections,
        categories: List<DiagnosticCategoryId> = DiagnosticCatalog.categories,
    ): RunAllPlan {
        require(categories.distinct().size == categories.size)
        val categoryPlans =
            categories.map { categoryId ->
                categoryPlan(categoryId, hardware, permissions, selections)
            }
        val automaticPrerequisites =
            if (DiagnosticCategoryId.AUDIO in categories && selections.includeMicrophone) {
                listOf(RunAllStage.AUTOMATIC)
            } else {
                emptyList()
            }
        val stages =
            (automaticPrerequisites + categoryPlans.mapNotNull(RunAllCategoryPlan::stage)).distinct() +
                RunAllStage.RESULTS
        return RunAllPlan(categories = categoryPlans, stages = stages)
    }

    private fun categoryPlan(
        categoryId: DiagnosticCategoryId,
        hardware: RunAllHardwareProfile,
        permissions: RunAllPermissions,
        selections: RunAllSelections,
    ): RunAllCategoryPlan =
        when (categoryId) {
            DiagnosticCategoryId.DEVICE,
            DiagnosticCategoryId.PERFORMANCE,
            DiagnosticCategoryId.BATTERY,
            DiagnosticCategoryId.THERMAL,
            DiagnosticCategoryId.STORAGE,
            -> automatic(categoryId)

            DiagnosticCategoryId.SIM ->
                automatic(
                    categoryId,
                    limited = !permissions.phone,
                )

            DiagnosticCategoryId.CONNECTIVITY ->
                automatic(
                    categoryId,
                    limited = !permissions.location || !permissions.bluetooth,
                )

            DiagnosticCategoryId.DISPLAY -> interactive(categoryId, RunAllStage.DISPLAY)
            DiagnosticCategoryId.AUDIO ->
                if (selections.includeSpeaker) {
                    interactive(categoryId, RunAllStage.AUDIO)
                } else {
                    automatic(
                        categoryId,
                        limited =
                            selections.includeMicrophone &&
                                (!hardware.microphoneAvailable || !permissions.microphone),
                    )
                }

            DiagnosticCategoryId.CAMERA ->
                when {
                    !selections.includeCamera -> skipped(categoryId)
                    !hardware.cameraAvailable -> notApplicable(categoryId)
                    !permissions.camera -> permissionLimited(categoryId)
                    else -> interactive(categoryId, RunAllStage.CAMERA)
                }

            DiagnosticCategoryId.SENSORS ->
                if (hardware.motionSensorAvailable) {
                    interactive(categoryId, RunAllStage.SENSORS)
                } else {
                    notApplicable(categoryId)
                }

            DiagnosticCategoryId.VIBRATION ->
                if (hardware.vibratorAvailable) {
                    interactive(categoryId, RunAllStage.VIBRATION)
                } else {
                    notApplicable(categoryId)
                }

            DiagnosticCategoryId.BUTTONS -> interactive(categoryId, RunAllStage.BUTTONS)
            DiagnosticCategoryId.BIOMETRICS ->
                if (hardware.biometricsAvailable) {
                    interactive(categoryId, RunAllStage.BIOMETRICS)
                } else {
                    notApplicable(categoryId)
                }
        }

    private fun automatic(
        categoryId: DiagnosticCategoryId,
        limited: Boolean = false,
    ) =
        RunAllCategoryPlan(
            categoryId = categoryId,
            stage = RunAllStage.AUTOMATIC,
            disposition =
                if (limited) {
                    RunAllCategoryDisposition.PERMISSION_LIMITED
                } else {
                    RunAllCategoryDisposition.AUTOMATIC
                },
        )

    private fun interactive(
        categoryId: DiagnosticCategoryId,
        stage: RunAllStage,
    ) = RunAllCategoryPlan(categoryId, stage, RunAllCategoryDisposition.INTERACTIVE)

    private fun permissionLimited(categoryId: DiagnosticCategoryId) =
        RunAllCategoryPlan(categoryId, null, RunAllCategoryDisposition.PERMISSION_LIMITED)

    private fun skipped(categoryId: DiagnosticCategoryId) =
        RunAllCategoryPlan(categoryId, null, RunAllCategoryDisposition.SKIPPED_BY_USER)

    private fun notApplicable(categoryId: DiagnosticCategoryId) =
        RunAllCategoryPlan(categoryId, null, RunAllCategoryDisposition.NOT_APPLICABLE)
}
