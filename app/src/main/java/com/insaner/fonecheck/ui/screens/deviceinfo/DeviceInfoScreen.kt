package com.insaner.fonecheck.ui.screens.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.RefreshButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DeviceInfoContent(
        state = state,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
internal fun DeviceInfoContent(
    state: DeviceInfoState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        if (state.isLoading && state.info == null) {
            Note(
                text = stringResource(R.string.device_loading),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        state.info?.let { info ->
            IdentitySection(info)
            OperatingSystemSection(info)
            DrmSection(info)
            SecuritySection(info)
        }

        state.error?.let {
            Note(
                text = stringResource(R.string.device_capture_error_description),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
            )
        }

        RefreshButton(
            label = stringResource(R.string.device_refresh),
            enabled = !state.isLoading,
            onClick = onRefresh,
        )

        state.info?.let { info ->
            Note(
                text = stringResource(R.string.device_captured_at, formatCapturedAt(info.capturedAt)),
            )
        }
    }
}

@Composable
private fun IdentitySection(info: DeviceInfo) {
    DeviceSection(label = stringResource(R.string.device_identity_title)) {
        DataRow(
            stringResource(R.string.label_model),
            availableDeviceValue(info.model),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        DataRow(
            stringResource(R.string.label_manufacturer),
            availableDeviceValue(info.manufacturer),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        DataRow(
            stringResource(R.string.label_brand),
            availableDeviceValue(info.brand),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        DataRow(
            stringResource(R.string.label_product),
            availableDeviceValue(info.product),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        DataRow(
            label = stringResource(R.string.label_serial_number),
            value = null,
            unavailableLabel = stringResource(R.string.device_value_restricted),
            showDivider = false,
        )
        Note(stringResource(R.string.device_serial_restricted_note))
        HairlineRule()
    }
}

@Composable
private fun OperatingSystemSection(info: DeviceInfo) {
    DeviceSection(label = stringResource(R.string.os_info_title)) {
        DataRow(
            stringResource(R.string.label_android_version),
            availableDeviceValue(info.androidVersion),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        DataRow(stringResource(R.string.label_api_level), info.apiLevel.toString())
        DataRow(
            stringResource(R.string.label_security_patch),
            availableDeviceValue(info.securityPatch),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        DataRow(
            stringResource(R.string.label_build_number),
            availableDeviceValue(info.buildNumber),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        LongValueRow(
            stringResource(R.string.label_kernel),
            splitConcatenatedDeviceIdentifiers(availableDeviceValue(info.kernelVersion)),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        LongValueRow(
            stringResource(R.string.label_baseband),
            splitConcatenatedDeviceIdentifiers(availableDeviceValue(info.basebandVersion)),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
        LongValueRow(
            stringResource(R.string.label_bootloader),
            splitConcatenatedDeviceIdentifiers(availableDeviceValue(info.bootloaderVersion)),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
    }
}

@Composable
private fun DrmSection(info: DeviceInfo) {
    DeviceSection(label = stringResource(R.string.drm_info_title)) {
        DataRow(
            stringResource(R.string.label_widevine),
            availableDeviceValue(info.widevineLevel),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
        )
    }
}

@Composable
private fun SecuritySection(info: DeviceInfo) {
    DeviceSection(label = stringResource(R.string.security_info_title)) {
        DataRow(
            label = stringResource(R.string.label_root_artifact),
            value =
                stringResource(
                    if (info.rootArtifactDetected) {
                        R.string.run_all_status_warning
                    } else {
                        R.string.run_all_status_pass
                    },
                ),
            tone = if (info.rootArtifactDetected) SemanticTone.ATTENTION else SemanticTone.PASS,
            showDivider = false,
        )
        if (info.rootArtifactDetected) {
            Note(stringResource(R.string.device_root_finding_note))
        }
        Note(stringResource(R.string.device_root_heuristic_disclaimer))
        HairlineRule()

        DataRow(
            label = stringResource(R.string.label_developer_options),
            value = enabledLabel(info.developerOptionsEnabled),
            showDivider = false,
        )
        Note(stringResource(R.string.device_developer_options_note))
        HairlineRule()

        DataRow(
            label = stringResource(R.string.label_usb_debugging),
            value = enabledLabel(info.usbDebuggingEnabled),
            showDivider = false,
        )
        Note(stringResource(R.string.device_usb_debugging_note))
        HairlineRule()
    }
}

@Composable
private fun DeviceSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label)
        content()
    }
}

@Composable
private fun enabledLabel(enabled: Boolean): String =
    stringResource(if (enabled) R.string.status_enabled else R.string.status_disabled)

internal fun availableDeviceValue(value: String): String? = value.takeUnless { it == DeviceInfo.UNAVAILABLE }

internal fun splitConcatenatedDeviceIdentifiers(value: String?): String? {
    if (value == null) return null
    listOf(',', ';').forEach { separator ->
        val parts = value.split(separator)
        if (parts.size == 2 && parts.all { it.isNotBlank() }) {
            return "${parts[0].trimEnd()}$separator\n${parts[1].trimStart()}"
        }
    }
    return value
}

private val capturedAtFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

internal fun formatCapturedAt(
    value: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = capturedAtFormatter.withZone(zoneId).format(value)
