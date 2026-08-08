package com.insaner.fonecheck.ui.screens.runall

import android.text.format.Formatter
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CategoryTestResult
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.TestResult
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.navigation.CategoryRetest
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.ConfidenceBadge
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresentation
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresenter
import com.insaner.fonecheck.ui.screens.report.stableCodeFallback
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral400
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import com.insaner.fonecheck.ui.theme.readableStatusColor
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

enum class ReportResultMode {
    COMPLETED_RUN,
    SAVED_REPORT,
}

@Composable
fun RunAllResultsScreen(
    report: DiagnosticReport,
    saveStatus: ReportSaveStatus,
    onRetrySave: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    mode: ReportResultMode = ReportResultMode.COMPLETED_RUN,
) {
    val presentation = remember(report) { ReportDetailPresenter.present(report) }
    val categories =
        presentation.categories.mapNotNull { category ->
            if (diagnosticDestinations.none { it.category == category.categoryId }) {
                null
            } else {
                category.toUiResult()
            }
        }
    val attentionResults =
        categories.filter {
            it.status is TestStatus.Fail || it.status is TestStatus.Warning
        }
    val completedResults =
        categories.filter {
            it.status == TestStatus.Pass || it.status is TestStatus.Info
        }
    val incompleteResults =
        categories.filter {
            it.status == TestStatus.NotAvailable || it.status == TestStatus.NotTested
        }
    var expandedCategoryName by rememberSaveable(attentionResults) {
        mutableStateOf(attentionResults.firstOrNull()?.category?.name)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (mode == ReportResultMode.SAVED_REPORT) {
            item {
                ReportMetadataCard(report, presentation.durationMillis)
            }
        }

        item {
            ResultsSummaryCard(report, presentation)
        }

        if (saveStatus != ReportSaveStatus.SAVED) {
            item {
                ReportSaveCard(saveStatus, onRetrySave)
            }
        }

        if (attentionResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_needs_attention),
                    count = attentionResults.size,
                )
            }
            categoryResultItems(
                results = attentionResults,
                expandedCategoryName = expandedCategoryName,
                onExpandedChange = { expandedCategoryName = it },
                onOpenCategory = onOpenCategory,
                mode = mode,
            )
        }

        if (completedResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_completed),
                    count = completedResults.size,
                )
            }
            item {
                CompletedResultsCard(
                    results = completedResults,
                    expandedCategoryName = expandedCategoryName,
                    onToggle = { result ->
                        expandedCategoryName = toggleExpanded(expandedCategoryName, result)
                    },
                    onOpenCategory = onOpenCategory,
                    mode = mode,
                )
            }
        }

        if (incompleteResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_not_completed),
                    count = incompleteResults.size,
                )
            }
            categoryResultItems(
                results = incompleteResults,
                expandedCategoryName = expandedCategoryName,
                onExpandedChange = { expandedCategoryName = it },
                onOpenCategory = onOpenCategory,
                mode = mode,
            )
        }

        item {
            Button(
                onClick = onDone,
                enabled = saveStatus == ReportSaveStatus.SAVED,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 20.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    stringResource(
                        if (mode == ReportResultMode.SAVED_REPORT) {
                            R.string.report_back
                        } else {
                            R.string.run_all_done
                        },
                    ),
                )
            }
        }
    }
}

private fun LazyListScope.categoryResultItems(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onExpandedChange: (String?) -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
) {
    items(
        items = results,
        key = { it.category.name },
    ) { result ->
        ExpandedCategoryResult(
            result = result,
            isExpanded = expandedCategoryName == result.category.name,
            onToggle = {
                onExpandedChange(toggleExpanded(expandedCategoryName, result))
            },
            onOpenCategory = onOpenCategory,
            mode = mode,
        )
    }
}

private fun toggleExpanded(
    currentCategoryName: String?,
    result: CategoryTestResult,
): String? = if (currentCategoryName == result.category.name) null else result.category.name

@Composable
private fun ResultsSummaryCard(
    report: DiagnosticReport,
    presentation: ReportDetailPresentation,
) {
    val score = report.score.value
    val counts = presentation.counts
    val scoreColor =
        when {
            score == null -> Neutral400
            score >= 85 -> Green400
            score >= 65 -> Yellow400
            else -> Red400
        }

    StandardCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.run_all_results_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.run_all_results_description),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = score?.toString() ?: "—",
                        style = MaterialTheme.typography.displayMedium,
                        color = readableStatusColor(scoreColor),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text =
                            stringResource(R.string.run_all_overall_score) +
                                " · " +
                                scoreStateLabel(report.score.state),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryCount(
                        stringResource(R.string.report_completed_count, counts.pass + counts.info),
                        Green400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_warning_count, counts.warning, counts.warning),
                        Yellow400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_failed_count, counts.fail, counts.fail),
                        Red400,
                    )
                    SummaryCount(
                        pluralStringResource(
                            R.plurals.run_all_unavailable_count,
                            counts.notAvailable,
                            counts.notAvailable,
                        ),
                        Neutral400,
                    )
                    SummaryCount(
                        stringResource(R.string.report_not_tested_count, counts.notTested),
                        Neutral400,
                    )
                }
            }
            Text(
                text =
                    stringResource(R.string.report_coverage) +
                        ": " +
                        stringResource(
                            R.string.report_coverage_value,
                            report.coverage.percentage,
                            report.coverage.completedCount,
                            report.coverage.applicableCount,
                        ),
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReportMetadataCard(
    report: DiagnosticReport,
    durationMillis: Long,
) {
    val dateFormatter =
        remember {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
        }
    val durationSeconds = durationMillis / 1_000L
    val deviceName =
        listOf(report.device.manufacturer, report.device.model)
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(" ")

    StandardCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.report_saved_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.report_saved_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InfoRow(
                label = stringResource(R.string.report_kind),
                value =
                    stringResource(
                        if (report.kind == ReportKind.FULL_CHECK) {
                            R.string.report_kind_full
                        } else {
                            R.string.report_kind_category
                        },
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_device),
                value = deviceName,
            )
            InfoRow(
                label = stringResource(R.string.report_android),
                value =
                    stringResource(
                        R.string.report_android_value,
                        report.device.androidRelease,
                        report.device.apiLevel,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_app_version),
                value =
                    stringResource(
                        R.string.report_app_version_value,
                        report.app.versionName,
                        report.app.versionCode,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_completed_at),
                value = dateFormatter.format(report.completedAt),
            )
            InfoRow(
                label = stringResource(R.string.report_duration),
                value =
                    stringResource(
                        R.string.report_duration_value,
                        durationSeconds / 60L,
                        durationSeconds % 60L,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_identifier),
                value = report.stableId,
            )
        }
    }
}

@Composable
private fun scoreStateLabel(state: ScoreState): String =
    stringResource(
        when (state) {
            ScoreState.COMPLETE -> R.string.report_score_complete
            ScoreState.PARTIAL -> R.string.report_score_partial
            ScoreState.INCOMPLETE -> R.string.report_score_incomplete
        },
    )

@Composable
private fun ReportSaveCard(
    status: ReportSaveStatus,
    onRetry: () -> Unit,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text =
                    stringResource(
                        if (status == ReportSaveStatus.FAILED) {
                            R.string.run_all_save_failed
                        } else {
                            R.string.run_all_saving_report
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (status == ReportSaveStatus.FAILED) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.run_all_retry_save))
                }
            }
        }
    }
}

@Composable
private fun DiagnosticCategoryResult.toUiResult(): CategoryTestResult =
    CategoryTestResult(
        category = categoryId,
        status = aggregateStatus.toLegacyStatus(),
        summary = categorySummary(aggregateStatus),
        results = evidence.map { it.toUiResult() },
    )

@Composable
private fun DiagnosticEvidence.toUiResult(): TestResult =
    TestResult(
        id = checkId.value,
        name = evidenceLabel(checkId.value),
        status = status.toLegacyStatus(),
        detail = evidenceDetail(this),
        confidence = confidence,
        timestamp = capturedAt.toEpochMilli(),
        source = source,
        reason = reason?.let { reasonLabel(it) },
    )

private fun DiagnosticStatus.toLegacyStatus(): TestStatus =
    when (this) {
        DiagnosticStatus.PASS -> TestStatus.Pass
        DiagnosticStatus.FAIL -> TestStatus.Fail()
        DiagnosticStatus.WARNING -> TestStatus.Warning()
        DiagnosticStatus.INFO -> TestStatus.Info("")
        DiagnosticStatus.NOT_AVAILABLE -> TestStatus.NotAvailable
        DiagnosticStatus.NOT_TESTED -> TestStatus.NotTested
    }

@Composable
private fun categorySummary(status: DiagnosticStatus): String =
    stringResource(
        when (status) {
            DiagnosticStatus.PASS, DiagnosticStatus.INFO -> R.string.run_all_summary_pass
            DiagnosticStatus.WARNING -> R.string.run_all_summary_warning
            DiagnosticStatus.FAIL -> R.string.run_all_summary_fail
            DiagnosticStatus.NOT_AVAILABLE -> R.string.run_all_summary_unavailable
            DiagnosticStatus.NOT_TESTED -> R.string.run_all_summary_not_tested
        },
    )

@Composable
private fun evidenceLabel(checkId: String): String = evidenceLabelResId(checkId)?.let { stringResource(it) } ?: checkId

@StringRes
private fun evidenceLabelResId(checkId: String): Int? =
    when (checkId) {
        "device.identity" -> R.string.device_info_title
        "device.security" -> R.string.label_root_artifact
        "performance.cpu" -> R.string.perf_cpu_title
        "performance.ram" -> R.string.perf_ram_title
        "performance.gpu" -> R.string.perf_gpu_title
        "performance.cpu_benchmark" -> R.string.perf_benchmark_cpu_rate
        "performance.memory_benchmark" -> R.string.perf_benchmark_memory_rate
        "sim.inventory" -> R.string.sim_telephony_title
        "sim.network" -> R.string.conn_mobile_network_type
        "display.info" -> R.string.display_info_title
        "display.visual" -> R.string.run_all_check_visual_display
        "audio.speaker" -> R.string.run_all_check_speaker
        "audio.microphone" -> R.string.run_all_check_microphone
        "audio.headphones" -> R.string.run_all_check_headphones
        "camera.rear" -> R.string.camera_rear
        "camera.front" -> R.string.camera_front
        "camera.capture" -> R.string.camera_capture_title
        "camera.inventory" -> R.string.camera_inventory_label
        "camera.logical_count" -> R.string.camera_logical_count_label
        "camera.capture_dimensions" -> R.string.camera_max_resolution
        "sensors.inventory" -> R.string.sensor_count
        "sensors.accelerometer" -> R.string.sensor_type_accelerometer
        "sensors.gyroscope" -> R.string.sensor_type_gyroscope
        "sensors.gravity" -> R.string.sensor_type_gravity
        "sensors.proximity" -> R.string.sensor_type_proximity
        "sensors.light" -> R.string.sensor_type_light
        "sensors.magnetometer" -> R.string.sensor_type_magnetometer
        "sensors.barometer" -> R.string.sensor_type_barometer
        "sensors.step" -> R.string.sensor_type_step
        "sensors.orientation" -> R.string.sensor_orientation
        "sensors.motion" -> R.string.run_all_check_motion_sensor
        "connectivity.wifi" -> R.string.conn_wifi_title
        "connectivity.bluetooth" -> R.string.conn_bluetooth_title
        "connectivity.nfc" -> R.string.conn_nfc_title
        "connectivity.nfc_hce" -> R.string.conn_nfc_hce
        "connectivity.gps" -> R.string.conn_gps_title
        "connectivity.mobile" -> R.string.conn_mobile_title
        "battery.health" -> R.string.batt_health_title
        "battery.temperature" -> R.string.batt_temperature
        "battery.level" -> R.string.batt_level
        "battery.current_now" -> R.string.batt_charging_current
        "battery.current_direction" -> R.string.batt_current_direction
        "battery.current_interpretation" -> R.string.batt_current_interpretation
        "battery.current_profile" -> R.string.batt_mfr_profile
        "battery.cycle_count" -> R.string.batt_cycle_count
        "thermal.status" -> R.string.thermal_status_title
        "thermal.severity" -> R.string.thermal_severity_label
        "thermal.headroom" -> R.string.thermal_headroom_title
        "thermal.battery_temperature" -> R.string.thermal_battery_title
        "storage.total" -> R.string.storage_total
        "storage.used" -> R.string.storage_used
        "storage.available" -> R.string.storage_available
        "storage.usage" -> R.string.storage_usage
        "storage.internal_access" -> R.string.storage_internal_access
        "storage.volume_count" -> R.string.storage_volumes_title
        "storage.mounted_volume_count" -> R.string.storage_mounted_state
        "storage.removable_volume_count" -> R.string.storage_removable
        "storage.sequential_write" -> R.string.storage_benchmark_write_rate
        "storage.sequential_read" -> R.string.storage_benchmark_read_rate
        "storage.benchmark_data_size" -> R.string.storage_benchmark_data_size
        "storage.benchmark_available_before" -> R.string.storage_benchmark_available_before
        "storage.benchmark_location" -> R.string.storage_benchmark_location
        "storage.benchmark_cleanup" -> R.string.storage_benchmark_cleanup
        "vibration.hardware" -> R.string.vibration_has_vibrator
        "vibration.amplitude_control" -> R.string.vibration_amplitude_control
        "vibration.effects" -> R.string.vibration_effects_supported
        "vibration.primitives" -> R.string.vibration_primitives_supported
        "vibration.motor" -> R.string.vibration_motor_title
        "buttons.volume" -> R.string.run_all_check_volume_buttons
        "buttons.power" -> R.string.button_power
        "biometrics.capability" -> R.string.biometric_capabilities_title
        "biometrics.fingerprint_hardware" -> R.string.biometric_fingerprint_hardware
        "biometrics.face_hardware" -> R.string.biometric_face_hardware
        "biometrics.strong_capability" -> R.string.biometric_strong
        "biometrics.weak_capability" -> R.string.biometric_weak
        "biometrics.authentication" -> R.string.biometric_test_auth
        else -> null
    }

@Composable
private fun evidenceDetail(evidence: DiagnosticEvidence): String? =
    evidence.value?.let { evidenceValueLabel(it, evidence.unit) }

@Composable
private fun evidenceValueLabel(
    value: EvidenceValue,
    unit: EvidenceUnitCode?,
): String =
    when (value) {
        is EvidenceValue.BooleanValue ->
            stringResource(if (value.value) R.string.status_yes else R.string.status_no)

        is EvidenceValue.IntValue ->
            when (unit?.value) {
                "percent" -> stringResource(R.string.storage_percent_value, localizedNumber(value.value))
                "samples" -> pluralStringResource(R.plurals.sensor_samples, value.value, value.value)
                else -> localizedNumber(value.value)
            }

        is EvidenceValue.LongValue ->
            when (unit?.value) {
                "operations_per_second" ->
                    stringResource(R.string.perf_benchmark_cpu_rate_value, localizedNumber(value.value))
                "milliseconds" ->
                    stringResource(R.string.conn_gps_fix_duration_format, value.value / 1_000.0)
                "bytes" -> Formatter.formatFileSize(LocalContext.current, value.value)
                else -> localizedNumber(value.value)
            }
        is EvidenceValue.DecimalValue -> localizedNumber(value.value)
        is EvidenceValue.DoubleValue ->
            when (unit?.value) {
                "celsius" -> stringResource(R.string.run_all_detail_temperature, value.value)
                "milliamperes" -> stringResource(R.string.batt_value_milliamps, value.value)
                "ratio" -> stringResource(R.string.thermal_headroom_value, value.value)
                "percent" -> stringResource(R.string.storage_percent_value, localizedNumber(value.value))
                "mebibytes_per_second" ->
                    stringResource(R.string.storage_rate_value, value.value)
                else -> localizedNumber(value.value)
            }

        is EvidenceValue.RawTextValue -> value.value
        is EvidenceValue.StableTextCodeValue -> stableTextLabel(value.value)
    }

private fun localizedNumber(value: Number): String =
    NumberFormat
        .getNumberInstance(Locale.getDefault())
        .apply {
            maximumFractionDigits = 6
        }.format(value)

@Composable
private fun stableTextLabel(code: String): String =
    when (code) {
        "good" -> R.string.batt_health_good
        "overheat" -> R.string.batt_health_overheat
        "dead" -> R.string.batt_health_dead
        "over_voltage" -> R.string.batt_health_over_voltage
        "unspecified_failure" -> R.string.batt_health_failure
        "cold" -> R.string.batt_health_cold
        "charging" -> R.string.batt_current_direction_charging
        "discharging" -> R.string.batt_current_direction_discharging
        "idle" -> R.string.batt_current_direction_idle
        "api_sign" -> R.string.batt_current_sign_api
        "status_sign_normalized" -> R.string.batt_current_sign_status
        "samsung" -> R.string.batt_mfr_samsung
        "oneplus" -> R.string.batt_mfr_oneplus
        "google_pixel" -> R.string.batt_mfr_pixel
        "generic" -> R.string.batt_mfr_generic
        "none" -> R.string.perf_thermal_none
        "normal" -> R.string.thermal_severity_normal
        "light" -> R.string.perf_thermal_light
        "moderate" -> R.string.perf_thermal_moderate
        "severe" -> R.string.perf_thermal_severe
        "critical" -> R.string.perf_thermal_critical
        "emergency" -> R.string.perf_thermal_emergency
        "shutdown" -> R.string.perf_thermal_shutdown
        "strong" -> R.string.biometric_strong
        "weak" -> R.string.biometric_weak
        "unavailable" -> R.string.biometric_not_available
        "available" -> R.string.biometric_available
        "no_hardware" -> R.string.biometric_no_hardware
        "hardware_unavailable" -> R.string.biometric_hardware_unavailable
        "none_enrolled" -> R.string.biometric_none_enrolled
        "security_update_required" -> R.string.biometric_security_update_required
        "unsupported" -> R.string.biometric_unsupported
        "known_artifact_detected" -> R.string.device_root_artifact_detected
        "no_known_artifact_detected" -> R.string.device_root_artifact_not_detected
        "no_telephony" -> R.string.sim_inventory_no_telephony
        "no_sim" -> R.string.sim_inventory_no_sim
        "inactive_sim" -> R.string.sim_inventory_inactive
        "single_sim" -> R.string.sim_inventory_single
        "multiple_sim" -> R.string.sim_inventory_multiple
        "second_generation" -> R.string.sim_network_2g
        "third_generation" -> R.string.sim_network_3g
        "fourth_generation" -> R.string.sim_network_4g
        "fifth_generation" -> R.string.sim_network_5g
        "unknown" -> R.string.sim_value_unknown
        "observed" -> R.string.sensor_orientation_observed
        "app_cache" -> R.string.storage_app_cache
        else -> null
    }?.let { stringResource(it) } ?: stableCodeFallback(code)

@Composable
private fun reasonLabel(reason: EvidenceReasonCode): String =
    evidenceReasonStringRes(reason)?.let { stringResource(it) } ?: stableCodeFallback(reason.value)

@Composable
private fun sourceLabel(source: EvidenceSource): String =
    stringResource(
        when (source) {
            EvidenceSource.AUTOMATIC_MEASUREMENT -> R.string.report_source_automatic
            EvidenceSource.ANDROID_API -> R.string.report_source_android_api
            EvidenceSource.USER_CONFIRMATION -> R.string.report_source_user
            EvidenceSource.DERIVED -> R.string.report_source_derived
            EvidenceSource.ESTIMATE -> R.string.report_source_estimate
        },
    )

@Composable
private fun SummaryCount(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = readableStatusColor(color),
    )
}

@Composable
private fun ResultSectionTitle(
    title: String,
    count: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandedCategoryResult(
    result: CategoryTestResult,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
) {
    val destination = diagnosticDestinations.first { it.category == result.category }
    CategoryResultCard(
        result = result,
        title = stringResource(destination.labelResId),
        imageResId = destination.imageResId,
        isExpanded = isExpanded,
        onToggle = onToggle,
        onOpen = {
            onOpenCategory(
                if (mode == ReportResultMode.SAVED_REPORT) {
                    CategoryRetest(result.category.stableId)
                } else {
                    destination.route
                },
            )
        },
        mode = mode,
    )
}

@Composable
private fun CompletedResultsCard(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onToggle: (CategoryTestResult) -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
) {
    StandardCard {
        results.forEachIndexed { index, result ->
            val destination = diagnosticDestinations.first { it.category == result.category }
            CompactResultRow(
                result = result,
                title = stringResource(destination.labelResId),
                imageResId = destination.imageResId,
                isExpanded = expandedCategoryName == result.category.name,
                onToggle = { onToggle(result) },
                onOpen = {
                    onOpenCategory(
                        if (mode == ReportResultMode.SAVED_REPORT) {
                            CategoryRetest(result.category.stableId)
                        } else {
                            destination.route
                        },
                    )
                },
                mode = mode,
            )
            if (index < results.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun CompactResultRow(
    result: CategoryTestResult,
    title: String,
    imageResId: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    mode: ReportResultMode,
) {
    val statusText = statusLabel(result.status)
    val expansionState =
        stringResource(
            if (isExpanded) R.string.accessibility_expanded else R.string.accessibility_collapsed,
        )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("report_category_${result.category.stableId}")
                .semantics {
                    role = Role.Button
                    stateDescription = "$statusText, $expansionState"
                }.clickable(onClick = onToggle)
                .padding(14.dp),
    ) {
        ResultHeader(
            result = result,
            title = title,
            imageResId = imageResId,
            showSummary = false,
            imageSize = 44.dp,
        )
        if (isExpanded) {
            ResultDetails(result.results, onOpen, mode)
        }
    }
}

@Composable
private fun CategoryResultCard(
    result: CategoryTestResult,
    title: String,
    imageResId: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    mode: ReportResultMode,
) {
    val statusText = statusLabel(result.status)
    val expansionState =
        stringResource(
            if (isExpanded) R.string.accessibility_expanded else R.string.accessibility_collapsed,
        )
    StandardCard(
        modifier =
            Modifier
                .testTag("report_category_${result.category.stableId}")
                .semantics { stateDescription = "$statusText, $expansionState" },
        onClick = onToggle,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultHeader(
                result = result,
                title = title,
                imageResId = imageResId,
                showSummary = true,
                imageSize = 52.dp,
            )
            Text(
                text =
                    stringResource(
                        when {
                            mode == ReportResultMode.SAVED_REPORT && isExpanded ->
                                R.string.report_hide_saved_evidence
                            mode == ReportResultMode.SAVED_REPORT -> R.string.report_view_saved_evidence
                            isExpanded -> R.string.run_all_hide_details
                            else -> R.string.run_all_view_details
                        },
                    ),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (isExpanded) {
                ResultDetails(result.results, onOpen, mode)
            }
        }
    }
}

@Composable
private fun ResultHeader(
    result: CategoryTestResult,
    title: String,
    imageResId: Int,
    showSummary: Boolean,
    imageSize: Dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = null,
            modifier = Modifier.size(imageSize),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            if (showSummary) {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StatusBadge(
            text = statusLabel(result.status),
            color = statusColor(result.status),
        )
    }
}

@Composable
private fun ResultDetails(
    results: List<TestResult>,
    onOpen: () -> Unit,
    mode: ReportResultMode,
) {
    Column {
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (results.isEmpty()) {
            Text(
                text = stringResource(R.string.report_no_saved_evidence),
                modifier = Modifier.padding(vertical = 14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            results.forEach { testResult ->
                ResultDetailRow(testResult)
            }
        }
        OutlinedButton(
            onClick = onOpen,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                stringResource(
                    if (mode == ReportResultMode.SAVED_REPORT) {
                        R.string.report_retest
                    } else {
                        R.string.run_all_open_test
                    },
                ),
            )
        }
    }
}

@Composable
private fun ResultDetailRow(result: TestResult) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            result.detail?.let { detail ->
                Text(
                    text = detail,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            result.reason?.let { reason ->
                Text(
                    text = stringResource(R.string.report_evidence_reason, reason),
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                result.source?.let { source ->
                    Text(
                        text = sourceLabel(source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ConfidenceBadge(confidence = result.confidence)
            }
        }
        StatusBadge(
            text = statusLabel(result.status),
            color = statusColor(result.status),
        )
    }
}

@Composable
private fun statusLabel(status: TestStatus): String =
    stringResource(
        when (status) {
            TestStatus.Pass -> R.string.run_all_status_pass
            is TestStatus.Warning -> R.string.run_all_status_warning
            is TestStatus.Fail -> R.string.run_all_status_fail
            is TestStatus.Info -> R.string.run_all_status_info
            TestStatus.NotAvailable -> R.string.run_all_status_unavailable
            TestStatus.NotTested -> R.string.run_all_status_not_tested
        },
    )

@Composable
private fun statusColor(status: TestStatus): Color =
    when (status) {
        TestStatus.Pass -> Green400
        is TestStatus.Warning -> Yellow400
        is TestStatus.Fail -> Red400
        is TestStatus.Info -> MaterialTheme.colorScheme.primary
        TestStatus.NotAvailable, TestStatus.NotTested -> Neutral400
    }
