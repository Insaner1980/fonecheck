package com.insaner.fonecheck.ui.screens.connectivity

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check", "kotlin:S3776")
fun ConnectivityTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ConnectivityTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bluetoothPermission =
        rememberPermissionController(
            kind = PermissionKind.BLUETOOTH,
            hardwareAvailable = state.bluetooth.isAvailable,
        )
    val locationPermission =
        rememberPermissionController(
            kind = PermissionKind.LOCATION,
            hardwareAvailable = state.gps.isAvailable,
        )
    val phonePermission =
        rememberPermissionController(
            kind = PermissionKind.PHONE,
            hardwareAvailable = state.mobileNetwork.isAvailable,
        )
    val bluetoothPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            bluetoothPermission.refresh()
            viewModel.onPermissionsGranted()
        }
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            locationPermission.refresh()
            viewModel.onPermissionsGranted()
        }
    val phonePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            phonePermission.refresh()
            viewModel.onPermissionsGranted()
        }

    LaunchedEffect(
        bluetoothPermission.state,
        locationPermission.state,
        phonePermission.state,
    ) {
        if (locationPermission.state != PermissionState.GRANTED) {
            viewModel.cancelGpsFix()
        }
        viewModel.onPermissionsGranted()
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::cancelGpsFix)
    }

    TestScreenContent(modifier = modifier) {
        // WiFi
        item {
            TestSectionCard(
                icon = "WiFi",
                title = stringResource(R.string.conn_wifi_title),
                statusText =
                    if (state.wifi.isConnected) {
                        stringResource(R.string.audio_connected)
                    } else if (state.wifi.isAvailable) {
                        stringResource(R.string.audio_disconnected)
                    } else {
                        stringResource(R.string.conn_not_available)
                    },
                statusColor =
                    when {
                        state.wifi.isConnected -> Green400
                        state.wifi.isAvailable -> Yellow400
                        else -> Red400
                    },
                isExpanded = state.expandedSection == ConnectivitySection.WIFI,
                onClick = { viewModel.toggleSection(ConnectivitySection.WIFI) },
            ) {
                WifiDetails(state.wifi)
            }
        }

        // Bluetooth
        item {
            TestSectionCard(
                icon = "BT",
                title = stringResource(R.string.conn_bluetooth_title),
                statusText =
                    when {
                        !state.bluetooth.isAvailable -> stringResource(R.string.conn_not_available)
                        bluetoothPermission.state != PermissionState.GRANTED &&
                            bluetoothPermission.state != PermissionState.NOT_REQUIRED ->
                            stringResource(R.string.run_all_permission_missing)
                        state.bluetooth.isEnabled == true -> stringResource(R.string.status_enabled)
                        else -> stringResource(R.string.status_disabled)
                    },
                statusColor =
                    when {
                        state.bluetooth.isEnabled == true -> Green400
                        state.bluetooth.isAvailable -> Yellow400
                        else -> Red400
                    },
                isExpanded = state.expandedSection == ConnectivitySection.BLUETOOTH,
                onClick = { viewModel.toggleSection(ConnectivitySection.BLUETOOTH) },
            ) {
                BluetoothDetails(
                    bluetooth = state.bluetooth,
                    permissionState = bluetoothPermission.state,
                    onRequestPermission = {
                        bluetoothPermission.onRequestLaunched()
                        bluetoothPermissionLauncher.launch(bluetoothPermission.permissions.toTypedArray())
                    },
                    onOpenSettings = bluetoothPermission::openSettings,
                )
            }
        }

        // NFC
        item {
            TestSectionCard(
                icon = "NFC",
                title = stringResource(R.string.conn_nfc_title),
                statusText =
                    when {
                        !state.nfc.isAvailable -> stringResource(R.string.conn_not_available)
                        state.nfc.isEnabled -> stringResource(R.string.status_enabled)
                        else -> stringResource(R.string.status_disabled)
                    },
                statusColor =
                    when {
                        state.nfc.isEnabled -> Green400
                        state.nfc.isAvailable -> Yellow400
                        else -> Red400
                    },
                isExpanded = state.expandedSection == ConnectivitySection.NFC,
                onClick = { viewModel.toggleSection(ConnectivitySection.NFC) },
            ) {
                NfcDetails(state.nfc)
            }
        }

        // GPS
        item {
            TestSectionCard(
                icon = "GPS",
                title = stringResource(R.string.conn_gps_title),
                statusText =
                    when (state.gps.fixStatus) {
                        GpsFixStatus.FIXED -> stringResource(R.string.conn_gps_fixed)
                        GpsFixStatus.SEARCHING -> stringResource(R.string.conn_gps_searching)
                        GpsFixStatus.FAILED -> stringResource(R.string.conn_gps_failed)
                        GpsFixStatus.NOT_STARTED -> {
                            when {
                                !state.gps.isAvailable -> stringResource(R.string.conn_not_available)
                                state.gps.isEnabled -> stringResource(R.string.conn_gps_ready)
                                else -> stringResource(R.string.status_disabled)
                            }
                        }
                    },
                statusColor =
                    when (state.gps.fixStatus) {
                        GpsFixStatus.FIXED -> Green400
                        GpsFixStatus.SEARCHING -> Yellow400
                        GpsFixStatus.FAILED ->
                            if (state.gps.failure == GpsFailureCode.TIMEOUT) Yellow400 else Red400
                        GpsFixStatus.NOT_STARTED -> {
                            if (state.gps.isAvailable && state.gps.isEnabled) {
                                Blue400
                            } else if (state.gps.isAvailable) {
                                Yellow400
                            } else {
                                Red400
                            }
                        }
                    },
                isExpanded = state.expandedSection == ConnectivitySection.GPS,
                onClick = { viewModel.toggleSection(ConnectivitySection.GPS) },
            ) {
                GpsDetails(
                    gps = state.gps,
                    permissionState = locationPermission.state,
                    viewModel = viewModel,
                    onRequestPermission = {
                        locationPermission.onRequestLaunched()
                        locationPermissionLauncher.launch(locationPermission.permissions.toTypedArray())
                    },
                    onOpenSettings = locationPermission::openSettings,
                )
            }
        }

        // Mobile Network
        item {
            TestSectionCard(
                icon = "CEL",
                title = stringResource(R.string.conn_mobile_title),
                statusText =
                    when {
                        !state.mobileNetwork.isAvailable -> stringResource(R.string.conn_not_available)
                        state.mobileNetwork.isConnected -> stringResource(R.string.audio_connected)
                        else -> stringResource(R.string.audio_disconnected)
                    },
                statusColor =
                    when {
                        state.mobileNetwork.isConnected -> Green400
                        state.mobileNetwork.isAvailable -> Yellow400
                        else -> Red400
                    },
                isExpanded = state.expandedSection == ConnectivitySection.MOBILE_NETWORK,
                onClick = { viewModel.toggleSection(ConnectivitySection.MOBILE_NETWORK) },
            ) {
                MobileNetworkDetails(
                    mobile = state.mobileNetwork,
                    permissionState = phonePermission.state,
                    onRequestPermission = {
                        phonePermission.onRequestLaunched()
                        phonePermissionLauncher.launch(phonePermission.permissions.toTypedArray())
                    },
                    onOpenSettings = phonePermission::openSettings,
                )
            }
        }
    }
}

// ── Detail InfoRow (local — bodySmall with weights, differs from shared InfoRow) ─

// ── WiFi Details ────────────────────────────────────────────────────────────────

@Composable
private fun WifiDetails(wifi: WifiState) {
    SectionBox {
        if (!wifi.isAvailable) {
            Text(
                text = stringResource(R.string.conn_wifi_not_supported),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
            return@SectionBox
        }

        if (!wifi.isConnected) {
            Text(
                text = stringResource(R.string.conn_wifi_not_connected),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
            return@SectionBox
        }

        wifi.ssid?.let { DetailInfoRow(stringResource(R.string.conn_wifi_ssid), it) }
        wifi.signalStrengthDbm?.let {
            DetailInfoRow(
                stringResource(R.string.conn_wifi_signal),
                "$it dBm",
                valueColor =
                    when {
                        it >= -50 -> Green400
                        it >= -60 -> Green400
                        it >= -70 -> Yellow400
                        else -> Red400
                    },
            )
        }
        wifi.signalLevel?.let {
            DetailInfoRow(stringResource(R.string.conn_wifi_signal_level), "$it / 4")
        }
        wifi.frequencyMhz?.let {
            val band =
                when {
                    it >= 5925 -> "6 GHz"
                    it >= 4900 -> "5 GHz"
                    it >= 2400 -> "2.4 GHz"
                    else -> null
                }
            DetailInfoRow(
                stringResource(R.string.conn_wifi_frequency),
                if (band == null) "$it MHz" else "$it MHz ($band)",
            )
        }
        wifi.linkSpeedMbps?.let {
            DetailInfoRow(stringResource(R.string.conn_wifi_link_speed), "$it Mbps")
        }
        wifi.wifiStandard?.let {
            DetailInfoRow(stringResource(R.string.conn_wifi_standard), it)
        }
        wifi.ipAddress?.let { DetailInfoRow(stringResource(R.string.conn_wifi_ip), it) }
        wifi.gateway?.let { DetailInfoRow(stringResource(R.string.conn_wifi_gateway), it) }
        wifi.dns1?.let { DetailInfoRow(stringResource(R.string.conn_wifi_dns1), it) }
        wifi.dns2?.let { DetailInfoRow(stringResource(R.string.conn_wifi_dns2), it) }
    }
}

// ── Bluetooth Details ───────────────────────────────────────────────────────────

@Composable
@Suppress("kotlin:S3776") // The card renders mutually exclusive Bluetooth states.
private fun BluetoothDetails(
    bluetooth: BluetoothState,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_bluetooth),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        SectionBox {
            if (!bluetooth.isAvailable) {
                Text(
                    text = stringResource(R.string.conn_bt_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500,
                )
                return@SectionBox
            }

            val canReadProtectedData =
                permissionState == PermissionState.GRANTED ||
                    permissionState == PermissionState.NOT_REQUIRED
            if (canReadProtectedData && bluetooth.isEnabled != null) {
                DetailInfoRow(
                    stringResource(R.string.conn_bt_status),
                    if (bluetooth.isEnabled == true) {
                        stringResource(R.string.status_enabled)
                    } else {
                        stringResource(R.string.status_disabled)
                    },
                    valueColor = if (bluetooth.isEnabled == true) Green400 else Yellow400,
                )
                bluetooth.name?.let { DetailInfoRow(stringResource(R.string.conn_bt_name), it) }
            }
            DetailInfoRow(
                stringResource(R.string.conn_bt_ble),
                if (bluetooth.bleSupported) {
                    stringResource(R.string.conn_supported)
                } else {
                    stringResource(R.string.conn_not_supported)
                },
                valueColor = if (bluetooth.bleSupported) Green400 else Neutral500,
            )
            if (canReadProtectedData) {
                DetailInfoRow(
                    stringResource(R.string.conn_bt_bonded),
                    "${bluetooth.bondedDeviceCount}",
                )
            }
        }
    }
}

// ── NFC Details ─────────────────────────────────────────────────────────────────

@Composable
private fun NfcDetails(nfc: NfcState) {
    SectionBox {
        if (!nfc.isAvailable) {
            Text(
                text = stringResource(R.string.conn_nfc_not_supported),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
            return@SectionBox
        }

        DetailInfoRow(
            stringResource(R.string.conn_nfc_status),
            if (nfc.isEnabled) {
                stringResource(R.string.status_enabled)
            } else {
                stringResource(R.string.status_disabled)
            },
            valueColor = if (nfc.isEnabled) Green400 else Red400,
        )
        DetailInfoRow(
            stringResource(R.string.conn_nfc_hce),
            if (nfc.supportsHostCardEmulation) {
                stringResource(R.string.conn_supported)
            } else {
                stringResource(R.string.conn_not_supported)
            },
            valueColor = if (nfc.supportsHostCardEmulation) Green400 else Neutral500,
        )
        Text(
            text = stringResource(R.string.conn_nfc_capability_only),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral500,
        )
    }
}

// ── GPS Details ─────────────────────────────────────────────────────────────────

@Composable
@Suppress("kotlin:S3776") // The card renders mutually exclusive GPS states.
private fun GpsDetails(
    gps: GpsState,
    permissionState: PermissionState,
    viewModel: ConnectivityTestViewModel,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_location),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        SectionBox {
            if (!gps.isAvailable) {
                Text(
                    text = stringResource(R.string.conn_gps_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500,
                )
                return@SectionBox
            }

            DetailInfoRow(
                stringResource(R.string.conn_gps_provider),
                if (gps.isEnabled) {
                    stringResource(R.string.status_enabled)
                } else {
                    stringResource(R.string.status_disabled)
                },
                valueColor = if (gps.isEnabled) Green400 else Red400,
            )

            when (gps.fixStatus) {
                GpsFixStatus.NOT_STARTED -> Unit
                GpsFixStatus.SEARCHING -> {
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_status),
                        stringResource(R.string.conn_gps_searching),
                        valueColor = Yellow400,
                    )
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_elapsed),
                        stringResource(
                            R.string.conn_gps_fix_duration_format,
                            uiNumber(gps.elapsedSearchMs / 1000.0, 1, 1),
                        ),
                    )
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_satellites_visible),
                        "${gps.satelliteCount}",
                    )
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_satellites_used),
                        "${gps.satellitesUsed}",
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        color = Yellow400,
                        trackColor = Neutral700,
                    )
                }
                GpsFixStatus.FIXED -> {
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_status),
                        stringResource(R.string.conn_gps_fixed),
                        valueColor = Green400,
                    )
                    gps.fixTimeMs?.let {
                        DetailInfoRow(
                            stringResource(R.string.conn_gps_fix_time),
                            stringResource(
                                R.string.conn_gps_fix_duration_format,
                                uiNumber(it / 1000.0, 1, 1),
                            ),
                            valueColor =
                                when {
                                    it < 5000 -> Green400
                                    it < 15000 -> Yellow400
                                    else -> Red400
                                },
                        )
                    }
                    gps.latitude?.let {
                        DetailInfoRow(
                            stringResource(R.string.conn_gps_latitude),
                            stringResource(R.string.conn_gps_coordinate_format, uiNumber(it, 6, 6)),
                        )
                    }
                    gps.longitude?.let {
                        DetailInfoRow(
                            stringResource(R.string.conn_gps_longitude),
                            stringResource(R.string.conn_gps_coordinate_format, uiNumber(it, 6, 6)),
                        )
                    }
                    gps.accuracy?.let {
                        DetailInfoRow(
                            stringResource(R.string.conn_gps_accuracy),
                            stringResource(R.string.conn_gps_accuracy_format, uiNumber(it, 1, 1)),
                            valueColor =
                                when {
                                    it < 5f -> Green400
                                    it < 20f -> Yellow400
                                    else -> Red400
                                },
                        )
                    }
                    gps.altitude?.let {
                        DetailInfoRow(
                            stringResource(R.string.conn_gps_altitude),
                            stringResource(R.string.conn_gps_altitude_format, uiNumber(it, 1, 1)),
                        )
                    }
                    gps.speed?.let {
                        DetailInfoRow(
                            stringResource(R.string.conn_gps_speed),
                            stringResource(R.string.conn_gps_speed_format, uiNumber(it, 1, 1)),
                        )
                    }
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_satellites_used),
                        stringResource(
                            R.string.conn_gps_satellite_ratio,
                            gps.satellitesUsed,
                            gps.satelliteCount,
                        ),
                    )
                }
                GpsFixStatus.FAILED -> {
                    DetailInfoRow(
                        stringResource(R.string.conn_gps_status),
                        stringResource(
                            if (gps.failure == GpsFailureCode.START_FAILED) {
                                R.string.conn_gps_start_failed
                            } else {
                                R.string.conn_gps_failed
                            },
                        ),
                        valueColor = if (gps.failure == GpsFailureCode.TIMEOUT) Yellow400 else Red400,
                    )
                }
            }
        }

        // Satellite list when we have them
        if (gps.satellites.isNotEmpty()) {
            SectionBox {
                Text(
                    text = stringResource(R.string.conn_gps_satellite_info),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "#",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500,
                        modifier = Modifier.weight(0.5f),
                    )
                    Text(
                        stringResource(R.string.conn_gps_constellation),
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "C/N\u2080",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.End,
                    )
                    Text(
                        stringResource(R.string.conn_gps_used),
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.End,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                gps.satellites.take(12).forEach { sat ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                    ) {
                        Text(
                            "${sat.svid}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.5f),
                        )
                        Text(
                            sat.constellation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            stringResource(R.string.conn_gps_cn0_format, uiNumber(sat.cn0DbHz, 1, 1)),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color =
                                when {
                                    sat.cn0DbHz >= 30 -> Green400
                                    sat.cn0DbHz >= 20 -> Yellow400
                                    else -> Red400
                                },
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.End,
                        )
                        Text(
                            if (sat.usedInFix) "\u2713" else "\u2013",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sat.usedInFix) Green400 else Neutral500,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.End,
                        )
                    }
                }
                if (gps.satellites.size > 12) {
                    val hiddenSatelliteCount = gps.satellites.size - 12
                    Text(
                        text =
                            pluralStringResource(
                                R.plurals.conn_gps_more_sats,
                                hiddenSatelliteCount,
                                hiddenSatelliteCount,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        // Action buttons
        if (gps.isAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (permissionState == PermissionState.GRANTED) {
                    when (gps.fixStatus) {
                        GpsFixStatus.NOT_STARTED, GpsFixStatus.FAILED -> {
                            Button(
                                onClick = { viewModel.startGpsFix() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                                shape = RoundedCornerShape(8.dp),
                                enabled = gps.isEnabled,
                            ) {
                                Text(stringResource(R.string.conn_gps_start_fix))
                            }
                        }
                        GpsFixStatus.SEARCHING -> {
                            OutlinedButton(
                                onClick = { viewModel.cancelGpsFix() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.audio_stop))
                            }
                        }
                        GpsFixStatus.FIXED -> {
                            Button(
                                onClick = { viewModel.startGpsFix() },
                                modifier = Modifier.weight(1f),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Neutral700,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.conn_gps_retest))
                            }
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.conn_gps_privacy_ephemeral),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
        }
    }
}

// ── Mobile Network Details ──────────────────────────────────────────────────────

@Composable
@Suppress("kotlin:S3776") // The card renders mutually exclusive mobile-network states.
private fun MobileNetworkDetails(
    mobile: MobileNetworkState,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_phone),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        SectionBox {
            if (!mobile.isAvailable) {
                Text(
                    text = stringResource(R.string.conn_mobile_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500,
                )
                return@SectionBox
            }

            DetailInfoRow(
                stringResource(R.string.conn_mobile_status),
                mobile.dataState?.displayName() ?: "\u2014",
                valueColor = if (mobile.isConnected) Green400 else null,
            )
            mobile.operatorName?.let {
                DetailInfoRow(stringResource(R.string.conn_mobile_operator), it)
            }
            mobile.simOperatorName?.let {
                DetailInfoRow(stringResource(R.string.conn_mobile_sim_operator), it)
            }
            mobile.phoneType?.let {
                DetailInfoRow(stringResource(R.string.conn_mobile_phone_type), it)
            }
            mobile.networkType?.let {
                DetailInfoRow(stringResource(R.string.conn_mobile_network_type), it)
            }
            mobile.signalStrengthDbm?.let {
                DetailInfoRow(
                    stringResource(R.string.conn_mobile_signal),
                    "$it dBm",
                    valueColor =
                        when {
                            it >= -80 -> Green400
                            it >= -100 -> Yellow400
                            else -> Red400
                        },
                )
            }
            mobile.signalLevel?.let {
                DetailInfoRow(stringResource(R.string.conn_mobile_signal_level), "$it / 4")
            }
            mobile.isRoaming?.let { isRoaming ->
                DetailInfoRow(
                    stringResource(R.string.conn_mobile_roaming),
                    if (isRoaming) {
                        stringResource(R.string.status_yes)
                    } else {
                        stringResource(R.string.status_no)
                    },
                    valueColor = if (isRoaming) Yellow400 else Green400,
                )
            }
            mobile.cellId?.let {
                DetailInfoRow(stringResource(R.string.conn_mobile_cell_id), it)
            }
            if (mobile.mcc != null && mobile.mnc != null) {
                DetailInfoRow(stringResource(R.string.conn_mobile_mcc_mnc), "${mobile.mcc}/${mobile.mnc}")
            }
        }
    }
}

@Composable
private fun MobileDataStateCode.displayName(): String =
    stringResource(
        when (this) {
            MobileDataStateCode.CONNECTED -> R.string.conn_mobile_data_connected
            MobileDataStateCode.CONNECTING -> R.string.conn_mobile_data_connecting
            MobileDataStateCode.DISCONNECTED -> R.string.conn_mobile_data_disconnected
            MobileDataStateCode.SUSPENDED -> R.string.conn_mobile_data_suspended
            MobileDataStateCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )
