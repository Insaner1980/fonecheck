package com.insaner.fonecheck.ui.screens.runall

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.TestResult
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral400
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
fun RunAllResultsScreen(
    report: DiagnosticReport,
    onOpenCategory: (Any) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories =
        report.categories.mapNotNull { category ->
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
        item {
            ResultsSummaryCard(report.score.value, categories)
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
            )
        }

        item {
            Button(
                onClick = onDone,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 20.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.run_all_done))
            }
        }
    }
}

private fun LazyListScope.categoryResultItems(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onExpandedChange: (String?) -> Unit,
    onOpenCategory: (Any) -> Unit,
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
        )
    }
}

private fun toggleExpanded(
    currentCategoryName: String?,
    result: CategoryTestResult,
): String? = if (currentCategoryName == result.category.name) null else result.category.name

@Composable
private fun ResultsSummaryCard(
    score: Int?,
    categories: List<CategoryTestResult>,
) {
    val passed = categories.count { it.status == TestStatus.Pass }
    val warnings = categories.count { it.status is TestStatus.Warning }
    val failed = categories.count { it.status is TestStatus.Fail }
    val unavailable =
        categories.count {
            it.status == TestStatus.NotAvailable || it.status == TestStatus.NotTested
        }
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
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.run_all_overall_score),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_passed_count, passed, passed),
                        Green400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_warning_count, warnings, warnings),
                        Yellow400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_failed_count, failed, failed),
                        Red400,
                    )
                    SummaryCount(
                        pluralStringResource(
                            R.plurals.run_all_unavailable_count,
                            unavailable,
                            unavailable,
                        ),
                        Neutral400,
                    )
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
private fun evidenceLabel(checkId: String): String =
    evidenceLabelResId(checkId)?.let { stringResource(it) } ?: checkId

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
        "sensors.inventory" -> R.string.sensor_count
        "sensors.motion" -> R.string.run_all_check_motion_sensor
        "connectivity.wifi" -> R.string.conn_wifi_title
        "connectivity.bluetooth" -> R.string.conn_bluetooth_title
        "connectivity.gps" -> R.string.conn_gps_title
        "connectivity.mobile" -> R.string.conn_mobile_title
        "battery.health" -> R.string.batt_health_title
        "battery.temperature" -> R.string.batt_temperature
        "battery.level" -> R.string.batt_level
        "vibration.hardware" -> R.string.vibration_has_vibrator
        "vibration.motor" -> R.string.vibration_motor_title
        "buttons.volume" -> R.string.run_all_check_volume_buttons
        "buttons.power" -> R.string.button_power
        "biometrics.capability" -> R.string.biometric_capabilities_title
        "biometrics.authentication" -> R.string.biometric_test_auth
        else -> null
    }

@Composable
private fun evidenceDetail(evidence: DiagnosticEvidence): String? =
    evidence.value?.let { evidenceValueLabel(it, evidence.unit) }
        ?: evidence.reason?.let { reasonLabel(it) }

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
                "percent" -> "${value.value}%"
                else -> value.value.toString()
            }

        is EvidenceValue.LongValue ->
            when (unit?.value) {
                "operations_per_second" ->
                    stringResource(R.string.perf_benchmark_cpu_rate_value, value.value.toString())
                else -> value.value.toString()
            }
        is EvidenceValue.DecimalValue -> value.value.toPlainString()
        is EvidenceValue.DoubleValue ->
            when (unit?.value) {
                "celsius" -> stringResource(R.string.run_all_detail_temperature, value.value)
                "mebibytes_per_second" ->
                    stringResource(R.string.perf_benchmark_memory_rate_value, value.value)
                else -> value.value.toString()
            }

        is EvidenceValue.RawTextValue -> value.value
        is EvidenceValue.StableTextCodeValue -> stableTextLabel(value.value)
    }

@Composable
private fun stableTextLabel(code: String): String =
    stringResource(
        when (code) {
            "good" -> R.string.batt_health_good
            "overheat" -> R.string.batt_health_overheat
            "dead" -> R.string.batt_health_dead
            "over_voltage" -> R.string.batt_health_over_voltage
            "unspecified_failure" -> R.string.batt_health_failure
            "cold" -> R.string.batt_health_cold
            "strong" -> R.string.biometric_strong
            "weak" -> R.string.biometric_weak
            "unavailable" -> R.string.biometric_not_available
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
            else -> R.string.batt_health_unknown
        },
    )

@Composable
private fun reasonLabel(reason: EvidenceReasonCode): String =
    stringResource(
        when (reason) {
            EvidenceReasonCode.PERMISSION_DENIED -> R.string.run_all_permission_missing
            EvidenceReasonCode.SKIPPED, EvidenceReasonCode.CANCELLED -> R.string.run_all_manual_skipped
            EvidenceReasonCode.USER_CONFIRMED_FAILURE -> R.string.run_all_manual_failed
            EvidenceReasonCode.HARDWARE_UNAVAILABLE,
            EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
            -> R.string.run_all_status_unavailable

            EvidenceReasonCode.DISABLED -> R.string.status_disabled
            EvidenceReasonCode.DEGRADED -> R.string.run_all_summary_warning
            EvidenceReasonCode.ERROR -> R.string.run_all_summary_fail
            else -> R.string.run_all_summary_not_tested
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
        color = color,
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
) {
    val destination = diagnosticDestinations.first { it.category == result.category }
    CategoryResultCard(
        result = result,
        title = stringResource(destination.labelResId),
        imageResId = destination.imageResId,
        isExpanded = isExpanded,
        onToggle = onToggle,
        onOpen = { onOpenCategory(destination.route) },
    )
}

@Composable
private fun CompletedResultsCard(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onToggle: (CategoryTestResult) -> Unit,
    onOpenCategory: (Any) -> Unit,
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
                onOpen = { onOpenCategory(destination.route) },
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
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
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
            ResultDetails(result.results, onOpen)
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
) {
    StandardCard(
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
                        if (isExpanded) R.string.run_all_hide_details else R.string.run_all_view_details,
                    ),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (isExpanded) {
                ResultDetails(result.results, onOpen)
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
) {
    Column {
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        results.forEach { testResult ->
            ResultDetailRow(testResult)
        }
        OutlinedButton(
            onClick = onOpen,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.run_all_open_test))
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
