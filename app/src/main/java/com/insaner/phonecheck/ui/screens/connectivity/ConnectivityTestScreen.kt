package com.insaner.phonecheck.ui.screens.connectivity

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.phonecheck.R
import com.insaner.phonecheck.ui.theme.Blue400
import com.insaner.phonecheck.ui.theme.Green400
import com.insaner.phonecheck.ui.theme.JetBrainsMono
import com.insaner.phonecheck.ui.theme.Neutral500
import com.insaner.phonecheck.ui.theme.Neutral700
import com.insaner.phonecheck.ui.theme.Neutral800
import com.insaner.phonecheck.ui.theme.Neutral850
import com.insaner.phonecheck.ui.theme.Red400
import com.insaner.phonecheck.ui.theme.Yellow400

@Composable
fun ConnectivityTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ConnectivityTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.onPermissionsGranted() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        // WiFi
        item {
            ConnectivityCard(
                icon = "WiFi",
                title = stringResource(R.string.conn_wifi_title),
                statusText = if (state.wifi.isConnected) stringResource(R.string.audio_connected)
                else if (state.wifi.isAvailable) stringResource(R.string.audio_disconnected)
                else stringResource(R.string.conn_not_available),
                statusColor = when {
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
            ConnectivityCard(
                icon = "BT",
                title = stringResource(R.string.conn_bluetooth_title),
                statusText = when {
                    !state.bluetooth.isAvailable -> stringResource(R.string.conn_not_available)
                    state.bluetooth.isEnabled -> stringResource(R.string.status_enabled)
                    else -> stringResource(R.string.status_disabled)
                },
                statusColor = when {
                    state.bluetooth.isEnabled -> Green400
                    state.bluetooth.isAvailable -> Yellow400
                    else -> Red400
                },
                isExpanded = state.expandedSection == ConnectivitySection.BLUETOOTH,
                onClick = { viewModel.toggleSection(ConnectivitySection.BLUETOOTH) },
            ) {
                BluetoothDetails(state.bluetooth)
            }
        }

        // NFC
        item {
            ConnectivityCard(
                icon = "NFC",
                title = stringResource(R.string.conn_nfc_title),
                statusText = when {
                    !state.nfc.isAvailable -> stringResource(R.string.conn_not_available)
                    state.nfc.isEnabled -> stringResource(R.string.status_enabled)
                    else -> stringResource(R.string.status_disabled)
                },
                statusColor = when {
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
            ConnectivityCard(
                icon = "GPS",
                title = stringResource(R.string.conn_gps_title),
                statusText = when (state.gps.fixStatus) {
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
                statusColor = when (state.gps.fixStatus) {
                    GpsFixStatus.FIXED -> Green400
                    GpsFixStatus.SEARCHING -> Yellow400
                    GpsFixStatus.FAILED -> Red400
                    GpsFixStatus.NOT_STARTED -> {
                        if (state.gps.isAvailable && state.gps.isEnabled) Blue400
                        else if (state.gps.isAvailable) Yellow400
                        else Red400
                    }
                },
                isExpanded = state.expandedSection == ConnectivitySection.GPS,
                onClick = { viewModel.toggleSection(ConnectivitySection.GPS) },
            ) {
                GpsDetails(state.gps, state.hasLocationPermission, viewModel, permissionLauncher)
            }
        }

        // Mobile Network
        item {
            ConnectivityCard(
                icon = "CEL",
                title = stringResource(R.string.conn_mobile_title),
                statusText = when {
                    !state.mobileNetwork.isAvailable -> stringResource(R.string.conn_not_available)
                    state.mobileNetwork.isConnected -> stringResource(R.string.audio_connected)
                    else -> stringResource(R.string.audio_disconnected)
                },
                statusColor = when {
                    state.mobileNetwork.isConnected -> Green400
                    state.mobileNetwork.isAvailable -> Yellow400
                    else -> Red400
                },
                isExpanded = state.expandedSection == ConnectivitySection.MOBILE_NETWORK,
                onClick = { viewModel.toggleSection(ConnectivitySection.MOBILE_NETWORK) },
            ) {
                MobileNetworkDetails(state.mobileNetwork, state.hasPhonePermission, permissionLauncher)
            }
        }
    }
}

// ── Shared Card ─────────────────────────────────────────────────────────────────

@Composable
private fun ConnectivityCard(
    icon: String,
    title: String,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Neutral800 else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpanded) Blue400.copy(alpha = 0.2f) else Neutral700),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isExpanded) Blue400 else Neutral500,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                StatusBadge(text = statusText, color = statusColor)
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
            ),
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun SectionBox(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Neutral850, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        content()
    }
}

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

        wifi.ssid?.let { InfoRow(stringResource(R.string.conn_wifi_ssid), it) }
        wifi.signalStrengthDbm?.let {
            InfoRow(
                stringResource(R.string.conn_wifi_signal),
                "$it dBm",
                valueColor = when {
                    it >= -50 -> Green400
                    it >= -60 -> Green400
                    it >= -70 -> Yellow400
                    else -> Red400
                },
            )
        }
        wifi.signalLevel?.let {
            InfoRow(stringResource(R.string.conn_wifi_signal_level), "$it / 4")
        }
        wifi.frequencyMhz?.let {
            val band = if (it >= 5000) "5 GHz" else if (it >= 4000) "4 GHz" else "2.4 GHz"
            InfoRow(stringResource(R.string.conn_wifi_frequency), "$it MHz ($band)")
        }
        wifi.linkSpeedMbps?.let {
            InfoRow(stringResource(R.string.conn_wifi_link_speed), "$it Mbps")
        }
        wifi.wifiStandard?.let {
            InfoRow(stringResource(R.string.conn_wifi_standard), it)
        }
        wifi.ipAddress?.let { InfoRow(stringResource(R.string.conn_wifi_ip), it) }
        wifi.gateway?.let { InfoRow(stringResource(R.string.conn_wifi_gateway), it) }
        wifi.dns1?.let { InfoRow(stringResource(R.string.conn_wifi_dns1), it) }
        wifi.dns2?.let { InfoRow(stringResource(R.string.conn_wifi_dns2), it) }
    }
}

// ── Bluetooth Details ───────────────────────────────────────────────────────────

@Composable
private fun BluetoothDetails(bluetooth: BluetoothState) {
    SectionBox {
        if (!bluetooth.isAvailable) {
            Text(
                text = stringResource(R.string.conn_bt_not_supported),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
            return@SectionBox
        }

        InfoRow(
            stringResource(R.string.conn_bt_status),
            if (bluetooth.isEnabled) stringResource(R.string.status_enabled)
            else stringResource(R.string.status_disabled),
            valueColor = if (bluetooth.isEnabled) Green400 else Red400,
        )
        bluetooth.name?.let { InfoRow(stringResource(R.string.conn_bt_name), it) }
        InfoRow(
            stringResource(R.string.conn_bt_ble),
            if (bluetooth.bleSupported) stringResource(R.string.conn_supported)
            else stringResource(R.string.conn_not_supported),
            valueColor = if (bluetooth.bleSupported) Green400 else Neutral500,
        )
        bluetooth.bluetoothVersion?.let {
            InfoRow(stringResource(R.string.conn_bt_version), it)
        }
        InfoRow(
            stringResource(R.string.conn_bt_bonded),
            "${bluetooth.bondedDeviceCount}",
        )
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

        InfoRow(
            stringResource(R.string.conn_nfc_status),
            if (nfc.isEnabled) stringResource(R.string.status_enabled)
            else stringResource(R.string.status_disabled),
            valueColor = if (nfc.isEnabled) Green400 else Red400,
        )
        InfoRow(
            stringResource(R.string.conn_nfc_hce),
            if (nfc.supportsHostCardEmulation) stringResource(R.string.conn_supported)
            else stringResource(R.string.conn_not_supported),
            valueColor = if (nfc.supportsHostCardEmulation) Green400 else Neutral500,
        )
    }
}

// ── GPS Details ─────────────────────────────────────────────────────────────────

@Composable
private fun GpsDetails(
    gps: GpsState,
    hasPermission: Boolean,
    viewModel: ConnectivityTestViewModel,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionBox {
            if (!gps.isAvailable) {
                Text(
                    text = stringResource(R.string.conn_gps_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500,
                )
                return@SectionBox
            }

            InfoRow(
                stringResource(R.string.conn_gps_provider),
                if (gps.isEnabled) stringResource(R.string.status_enabled)
                else stringResource(R.string.status_disabled),
                valueColor = if (gps.isEnabled) Green400 else Red400,
            )

            when (gps.fixStatus) {
                GpsFixStatus.NOT_STARTED -> {}
                GpsFixStatus.SEARCHING -> {
                    InfoRow(
                        stringResource(R.string.conn_gps_status),
                        stringResource(R.string.conn_gps_searching),
                        valueColor = Yellow400,
                    )
                    InfoRow(
                        stringResource(R.string.conn_gps_elapsed),
                        "%.1f s".format(gps.elapsedSearchMs / 1000f),
                    )
                    InfoRow(
                        stringResource(R.string.conn_gps_satellites_visible),
                        "${gps.satelliteCount}",
                    )
                    InfoRow(
                        stringResource(R.string.conn_gps_satellites_used),
                        "${gps.satellitesUsed}",
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Yellow400,
                        trackColor = Neutral700,
                    )
                }
                GpsFixStatus.FIXED -> {
                    InfoRow(
                        stringResource(R.string.conn_gps_status),
                        stringResource(R.string.conn_gps_fixed),
                        valueColor = Green400,
                    )
                    gps.fixTimeMs?.let {
                        InfoRow(
                            stringResource(R.string.conn_gps_fix_time),
                            "%.1f s".format(it / 1000f),
                            valueColor = when {
                                it < 5000 -> Green400
                                it < 15000 -> Yellow400
                                else -> Red400
                            },
                        )
                    }
                    gps.latitude?.let {
                        InfoRow(stringResource(R.string.conn_gps_latitude), "%.6f°".format(it))
                    }
                    gps.longitude?.let {
                        InfoRow(stringResource(R.string.conn_gps_longitude), "%.6f°".format(it))
                    }
                    gps.accuracy?.let {
                        InfoRow(
                            stringResource(R.string.conn_gps_accuracy),
                            "±%.1f m".format(it),
                            valueColor = when {
                                it < 5f -> Green400
                                it < 20f -> Yellow400
                                else -> Red400
                            },
                        )
                    }
                    gps.altitude?.let {
                        InfoRow(stringResource(R.string.conn_gps_altitude), "%.1f m".format(it))
                    }
                    gps.speed?.let {
                        InfoRow(stringResource(R.string.conn_gps_speed), "%.1f m/s".format(it))
                    }
                    InfoRow(
                        stringResource(R.string.conn_gps_satellites_used),
                        "${gps.satellitesUsed} / ${gps.satelliteCount}",
                    )
                }
                GpsFixStatus.FAILED -> {
                    InfoRow(
                        stringResource(R.string.conn_gps_status),
                        stringResource(R.string.conn_gps_failed),
                        valueColor = Red400,
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "#", style = MaterialTheme.typography.labelSmall,
                        color = Neutral500, modifier = Modifier.weight(0.5f),
                    )
                    Text(
                        stringResource(R.string.conn_gps_constellation),
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500, modifier = Modifier.weight(1f),
                    )
                    Text(
                        "C/N₀", style = MaterialTheme.typography.labelSmall,
                        color = Neutral500, modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.End,
                    )
                    Text(
                        stringResource(R.string.conn_gps_used),
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500, modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.End,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                gps.satellites.take(12).forEach { sat ->
                    Row(
                        modifier = Modifier
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
                            "%.1f".format(sat.cn0DbHz),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color = when {
                                sat.cn0DbHz >= 30 -> Green400
                                sat.cn0DbHz >= 20 -> Yellow400
                                else -> Red400
                            },
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.End,
                        )
                        Text(
                            if (sat.usedInFix) "✓" else "–",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sat.usedInFix) Green400 else Neutral500,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.End,
                        )
                    }
                }
                if (gps.satellites.size > 12) {
                    Text(
                        text = stringResource(R.string.conn_gps_more_sats, gps.satellites.size - 12),
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
                if (!hasPermission) {
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.conn_grant_location))
                    }
                } else {
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
                                onClick = { viewModel.stopGpsFix() },
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
                                colors = ButtonDefaults.buttonColors(containerColor = Neutral700),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.conn_gps_retest))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Mobile Network Details ──────────────────────────────────────────────────────

@Composable
private fun MobileNetworkDetails(
    mobile: MobileNetworkState,
    hasPhonePermission: Boolean,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionBox {
            if (!mobile.isAvailable) {
                Text(
                    text = stringResource(R.string.conn_mobile_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500,
                )
                return@SectionBox
            }

            InfoRow(
                stringResource(R.string.conn_mobile_status),
                mobile.dataState ?: "—",
                valueColor = if (mobile.isConnected) Green400 else null,
            )
            mobile.operatorName?.let {
                InfoRow(stringResource(R.string.conn_mobile_operator), it)
            }
            mobile.simOperatorName?.let {
                InfoRow(stringResource(R.string.conn_mobile_sim_operator), it)
            }
            mobile.phoneType?.let {
                InfoRow(stringResource(R.string.conn_mobile_phone_type), it)
            }
            mobile.networkType?.let {
                InfoRow(stringResource(R.string.conn_mobile_network_type), it)
            }
            mobile.signalStrengthDbm?.let {
                InfoRow(
                    stringResource(R.string.conn_mobile_signal),
                    "$it dBm",
                    valueColor = when {
                        it >= -80 -> Green400
                        it >= -100 -> Yellow400
                        else -> Red400
                    },
                )
            }
            mobile.signalLevel?.let {
                InfoRow(stringResource(R.string.conn_mobile_signal_level), "$it / 4")
            }
            InfoRow(
                stringResource(R.string.conn_mobile_roaming),
                if (mobile.isRoaming) stringResource(R.string.status_yes)
                else stringResource(R.string.status_no),
                valueColor = if (mobile.isRoaming) Yellow400 else Green400,
            )
            mobile.cellId?.let {
                InfoRow(stringResource(R.string.conn_mobile_cell_id), it)
            }
            if (mobile.mcc != null && mobile.mnc != null) {
                InfoRow(stringResource(R.string.conn_mobile_mcc_mnc), "${mobile.mcc}/${mobile.mnc}")
            }
        }

        if (!hasPhonePermission && mobile.isAvailable) {
            Button(
                onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.conn_grant_phone))
            }
        }
    }
}
