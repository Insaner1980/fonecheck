package com.insaner.fonecheck.ui.screens.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.StatusRow
import com.insaner.fonecheck.ui.theme.Red400
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.isLoading && state.info == null) {
            CircularProgressIndicator()
        }

        state.info?.let { info ->
            InfoCard(title = stringResource(R.string.device_info_title)) {
                InfoRow(stringResource(R.string.label_model), deviceValue(info.model))
                InfoRow(stringResource(R.string.label_manufacturer), deviceValue(info.manufacturer))
                InfoRow(stringResource(R.string.label_brand), deviceValue(info.brand))
                InfoRow(stringResource(R.string.label_product), deviceValue(info.product))
                InfoRow(
                    stringResource(R.string.device_captured_at),
                    formatCapturedAt(info.capturedAt),
                )
            }

            InfoCard(title = stringResource(R.string.os_info_title)) {
                InfoRow(stringResource(R.string.label_android_version), deviceValue(info.androidVersion))
                InfoRow(stringResource(R.string.label_api_level), info.apiLevel.toString())
                InfoRow(stringResource(R.string.label_security_patch), deviceValue(info.securityPatch))
                InfoRow(stringResource(R.string.label_build_number), deviceValue(info.buildNumber))
                InfoRow(stringResource(R.string.label_kernel), deviceValue(info.kernelVersion))
                InfoRow(stringResource(R.string.label_baseband), deviceValue(info.basebandVersion))
                InfoRow(stringResource(R.string.label_bootloader), deviceValue(info.bootloaderVersion))
            }

            InfoCard(title = stringResource(R.string.drm_info_title)) {
                InfoRow(stringResource(R.string.label_widevine), deviceValue(info.widevineLevel))
            }

            InfoCard(title = stringResource(R.string.security_info_title)) {
                StatusRow(
                    label = stringResource(R.string.label_root_artifact),
                    value =
                        stringResource(
                            if (info.rootArtifactDetected) {
                                R.string.device_root_artifact_detected
                            } else {
                                R.string.device_root_artifact_not_detected
                            },
                        ),
                    isHighlighted = info.rootArtifactDetected,
                )
                Text(
                    text = stringResource(R.string.device_root_heuristic_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                StatusRow(
                    label = stringResource(R.string.label_developer_options),
                    value =
                        if (info.developerOptionsEnabled) {
                            stringResource(R.string.status_enabled)
                        } else {
                            stringResource(R.string.status_disabled)
                        },
                    isHighlighted = info.developerOptionsEnabled,
                )
                StatusRow(
                    label = stringResource(R.string.label_usb_debugging),
                    value =
                        if (info.usbDebuggingEnabled) {
                            stringResource(R.string.status_enabled)
                        } else {
                            stringResource(R.string.status_disabled)
                        },
                    isHighlighted = info.usbDebuggingEnabled,
                )
            }
        }

        state.error?.let {
            InfoCard(title = stringResource(R.string.device_capture_error_title)) {
                Text(
                    text = stringResource(R.string.device_capture_error_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = Red400,
                )
            }
        }

        Button(
            onClick = viewModel::refresh,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.device_refresh))
        }
    }
}

@Composable
private fun deviceValue(value: String): String =
    if (value == DeviceInfo.UNAVAILABLE) {
        stringResource(R.string.device_value_unavailable)
    } else {
        value
    }

private fun formatCapturedAt(value: Instant): String =
    DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(value)
