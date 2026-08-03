package com.insaner.fonecheck.ui.screens.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.StatusRow

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val info = viewModel.deviceInfo

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Device Info Card
        InfoCard(title = stringResource(R.string.device_info_title)) {
            InfoRow(stringResource(R.string.label_model), info.model)
            InfoRow(stringResource(R.string.label_manufacturer), info.manufacturer)
            InfoRow(stringResource(R.string.label_brand), info.brand)
            InfoRow(stringResource(R.string.label_product), info.product)
        }

        // OS Info Card
        InfoCard(title = stringResource(R.string.os_info_title)) {
            InfoRow(stringResource(R.string.label_android_version), info.androidVersion)
            InfoRow(stringResource(R.string.label_api_level), info.apiLevel.toString())
            InfoRow(stringResource(R.string.label_security_patch), info.securityPatch)
            InfoRow(stringResource(R.string.label_build_number), info.buildNumber)
            InfoRow(stringResource(R.string.label_kernel), info.kernelVersion)
            InfoRow(stringResource(R.string.label_baseband), info.basebandVersion)
            InfoRow(stringResource(R.string.label_bootloader), info.bootloaderVersion)
        }

        // DRM Info Card
        InfoCard(title = stringResource(R.string.drm_info_title)) {
            InfoRow(stringResource(R.string.label_widevine), info.widevineLevel)
        }

        // Security Card
        InfoCard(title = stringResource(R.string.security_info_title)) {
            StatusRow(
                label = stringResource(R.string.label_root_detected),
                value = if (info.isRooted) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
                isHighlighted = info.isRooted,
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
}
