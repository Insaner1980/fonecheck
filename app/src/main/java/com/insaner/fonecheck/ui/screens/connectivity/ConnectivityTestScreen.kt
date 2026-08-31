package com.insaner.fonecheck.ui.screens.connectivity

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.classification.classifyGpsFix
import com.insaner.fonecheck.ui.classification.classifyGpsProvider
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureSection
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check", "kotlin:S3776")
fun ConnectivityTestScreen(
    modifier: Modifier = Modifier,
    onTopBarActionChange: (TopBarAction?) -> Unit = {},
    viewModel: ConnectivityTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
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

    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.conn_refresh,
        enabled = state.gps.fixStatus != GpsFixStatus.SEARCHING,
        onRefresh = viewModel::refreshAll,
        onTopBarActionChange = onTopBarActionChange,
    )

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

    TestScreenContent(modifier = modifier, liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis) {
        item {
            ConnectivitySectionBlock(
                title = stringResource(R.string.conn_wifi_title),
                status = wifiSummary(state.wifi),
                isExpanded = state.expandedSection == ConnectivitySection.WIFI,
                onToggle = { viewModel.toggleSection(ConnectivitySection.WIFI) },
            ) {
                WifiDetails(state.wifi)
            }
        }

        item {
            ConnectivitySectionBlock(
                title = stringResource(R.string.conn_bluetooth_title),
                status = bluetoothSummary(state.bluetooth, bluetoothPermission.state),
                isExpanded = state.expandedSection == ConnectivitySection.BLUETOOTH,
                onToggle = { viewModel.toggleSection(ConnectivitySection.BLUETOOTH) },
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

        item {
            ConnectivitySectionBlock(
                title = stringResource(R.string.conn_nfc_title),
                status = nfcSummary(state.nfc),
                isExpanded = state.expandedSection == ConnectivitySection.NFC,
                onToggle = { viewModel.toggleSection(ConnectivitySection.NFC) },
            ) {
                NfcDetails(state.nfc)
            }
        }

        item {
            ConnectivitySectionBlock(
                title = stringResource(R.string.conn_gps_title),
                status = gpsSummary(state.gps),
                statusTone = state.gps.summaryTone(),
                isExpanded = state.expandedSection == ConnectivitySection.GPS,
                onToggle = { viewModel.toggleSection(ConnectivitySection.GPS) },
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

        item {
            ConnectivitySectionBlock(
                title = stringResource(R.string.conn_mobile_title),
                status = mobileSummary(state.mobileNetwork),
                isExpanded = state.expandedSection == ConnectivitySection.MOBILE_NETWORK,
                onToggle = { viewModel.toggleSection(ConnectivitySection.MOBILE_NETWORK) },
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

@Composable
private fun ConnectivitySectionBlock(
    title: String,
    status: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    statusTone: SemanticTone = SemanticTone.NEUTRAL,
    content: @Composable () -> Unit,
) = DisclosureSection(
    label = title,
    summary = status,
    expanded = isExpanded,
    onClick = onToggle,
    tone = statusTone,
    content = content,
)

@Composable
private fun WifiDetails(wifi: WifiState) {
    Column {
        when {
            !wifi.isAvailable -> Note(text = stringResource(R.string.conn_wifi_not_supported))
            !wifi.isConnected -> Note(text = stringResource(R.string.conn_wifi_not_connected))
            else -> {
                LongValueRow(
                    label = stringResource(R.string.conn_wifi_ssid),
                    value = wifi.ssid,
                )
                DataRow(
                    label = stringResource(R.string.conn_wifi_signal),
                    value = wifi.signalStrengthDbm?.let { stringResource(R.string.conn_dbm_format, uiNumber(it)) },
                )
                DataRow(
                    label = stringResource(R.string.conn_wifi_signal_level),
                    value =
                        wifi.signalLevel?.let {
                            stringResource(
                                R.string.conn_signal_level_format,
                                uiNumber(it),
                                uiNumber(MAX_SIGNAL_LEVEL),
                            )
                        },
                )
                LongValueRow(
                    label = stringResource(R.string.conn_wifi_frequency),
                    value = wifi.frequencyMhz?.let { wifiFrequency(it) },
                )
                DataRow(
                    label = stringResource(R.string.conn_wifi_link_speed),
                    value = wifi.linkSpeedMbps?.let { stringResource(R.string.conn_mbps_format, uiNumber(it)) },
                )
                LongValueRow(
                    label = stringResource(R.string.conn_wifi_standard),
                    value = wifi.wifiStandard,
                )
                LongValueRow(label = stringResource(R.string.conn_wifi_ip), value = wifi.ipAddress)
                LongValueRow(label = stringResource(R.string.conn_wifi_gateway), value = wifi.gateway)
                LongValueRow(label = stringResource(R.string.conn_wifi_dns1), value = wifi.dns1)
                LongValueRow(label = stringResource(R.string.conn_wifi_dns2), value = wifi.dns2)
            }
        }
    }
}

@Composable
@Suppress("kotlin:S3776") // The section renders mutually exclusive Bluetooth states.
private fun BluetoothDetails(
    bluetooth: BluetoothState,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md)) {
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_bluetooth),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        if (!bluetooth.isAvailable) {
            Note(text = stringResource(R.string.conn_bt_not_supported))
            return@Column
        }

        val canReadProtectedData =
            permissionState == PermissionState.GRANTED ||
                permissionState == PermissionState.NOT_REQUIRED
        DataRow(
            label = stringResource(R.string.conn_bt_status),
            value =
                bluetooth.isEnabled?.takeIf { canReadProtectedData }?.let {
                    if (it) stringResource(R.string.status_enabled) else stringResource(R.string.status_disabled)
                },
        )
        LongValueRow(
            label = stringResource(R.string.conn_bt_name),
            value = if (canReadProtectedData) bluetooth.name else null,
        )
        DataRow(
            label = stringResource(R.string.conn_bt_ble),
            value =
                if (bluetooth.bleSupported) {
                    stringResource(R.string.conn_supported)
                } else {
                    stringResource(R.string.conn_not_supported)
                },
        )
        DataRow(
            label = stringResource(R.string.conn_bt_bonded),
            value = uiNumber(bluetooth.bondedDeviceCount).takeIf { canReadProtectedData },
        )
    }
}

@Composable
private fun NfcDetails(nfc: NfcState) {
    Column {
        if (!nfc.isAvailable) {
            Note(text = stringResource(R.string.conn_nfc_not_supported))
            return@Column
        }
        DataRow(
            label = stringResource(R.string.conn_nfc_status),
            value =
                if (nfc.isEnabled) {
                    stringResource(R.string.status_enabled)
                } else {
                    stringResource(R.string.status_disabled)
                },
        )
        DataRow(
            label = stringResource(R.string.conn_nfc_hce),
            value =
                if (nfc.supportsHostCardEmulation) {
                    stringResource(R.string.conn_supported)
                } else {
                    stringResource(R.string.conn_not_supported)
                },
        )
        Note(text = stringResource(R.string.conn_nfc_capability_only))
    }
}

@Composable
@Suppress("kotlin:S3776") // The section renders mutually exclusive GPS states.
private fun GpsDetails(
    gps: GpsState,
    permissionState: PermissionState,
    viewModel: ConnectivityTestViewModel,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md)) {
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_location),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        if (!gps.isAvailable) {
            Note(text = stringResource(R.string.conn_gps_not_supported))
            return@Column
        }

        val providerClassification = classifyGpsProvider(gps.isEnabled)
        DataRow(
            label = stringResource(R.string.conn_gps_provider),
            value =
                if (gps.isEnabled) {
                    stringResource(R.string.status_enabled)
                } else {
                    stringResource(R.string.status_disabled)
                },
        )
        ObservationReasonNote(providerClassification)

        when (gps.fixStatus) {
            GpsFixStatus.NOT_STARTED -> Unit
            GpsFixStatus.SEARCHING -> SearchingGpsRows(gps)
            GpsFixStatus.FIXED -> FixedGpsRows(gps)
            GpsFixStatus.FAILED -> FailedGpsRow(gps.failure)
        }

        if (gps.satellites.isNotEmpty()) {
            SatelliteSection(gps.satellites)
        }

        if (permissionState == PermissionState.GRANTED) {
            when (gps.fixStatus) {
                GpsFixStatus.NOT_STARTED, GpsFixStatus.FAILED -> {
                    PrimaryButton(
                        label = stringResource(R.string.conn_gps_start_fix),
                        onClick = viewModel::startGpsFix,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = gps.isEnabled,
                    )
                }
                GpsFixStatus.SEARCHING -> {
                    SecondaryButton(
                        label = stringResource(R.string.audio_stop),
                        onClick = viewModel::cancelGpsFix,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                GpsFixStatus.FIXED -> {
                    SecondaryButton(
                        label = stringResource(R.string.conn_gps_retest),
                        onClick = viewModel::startGpsFix,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Note(text = stringResource(R.string.conn_gps_privacy_ephemeral))
    }
}

@Composable
private fun SearchingGpsRows(gps: GpsState) {
    val classification = classifyGpsFix(gps.fixStatus, gps.failure)
    DataRow(
        label = stringResource(R.string.conn_gps_status),
        value = stringResource(R.string.conn_gps_searching),
        tone = classification.toSemanticTone(),
    )
    ObservationReasonNote(classification)
    DataRow(
        label = stringResource(R.string.conn_gps_elapsed),
        value =
            stringResource(
                R.string.conn_gps_fix_duration_format,
                uiNumber(gps.elapsedSearchMs / 1000.0, 1, 1),
            ),
    )
    DataRow(
        label = stringResource(R.string.conn_gps_satellites_visible),
        value = uiNumber(gps.satelliteCount),
    )
    DataRow(
        label = stringResource(R.string.conn_gps_satellites_used),
        value = uiNumber(gps.satellitesUsed),
    )
    Spacer(modifier = Modifier.height(FonecheckTheme.spacing.xs))
    IndeterminateRule()
}

@Composable
private fun FixedGpsRows(gps: GpsState) {
    val classification = classifyGpsFix(gps.fixStatus, gps.failure)
    DataRow(
        label = stringResource(R.string.conn_gps_status),
        value = stringResource(R.string.conn_gps_fixed),
        tone = classification.toSemanticTone(),
    )
    gps.fixTimeMs?.let {
        DataRow(
            label = stringResource(R.string.conn_gps_fix_time),
            value =
                stringResource(
                    R.string.conn_gps_fix_duration_format,
                    uiNumber(it / 1000.0, 1, 1),
                ),
        )
    }
    gps.latitude?.let {
        LongValueRow(
            label = stringResource(R.string.conn_gps_latitude),
            value = stringResource(R.string.conn_gps_coordinate_format, uiNumber(it, 6, 6)),
        )
    }
    gps.longitude?.let {
        LongValueRow(
            label = stringResource(R.string.conn_gps_longitude),
            value = stringResource(R.string.conn_gps_coordinate_format, uiNumber(it, 6, 6)),
        )
    }
    gps.accuracy?.let {
        DataRow(
            label = stringResource(R.string.conn_gps_accuracy),
            value = stringResource(R.string.conn_gps_accuracy_format, uiNumber(it, 1, 1)),
        )
    }
    gps.altitude?.let {
        DataRow(
            label = stringResource(R.string.conn_gps_altitude),
            value = stringResource(R.string.conn_gps_altitude_format, uiNumber(it, 1, 1)),
        )
    }
    gps.speed?.let {
        DataRow(
            label = stringResource(R.string.conn_gps_speed),
            value = stringResource(R.string.conn_gps_speed_format, uiNumber(it, 1, 1)),
        )
    }
    DataRow(
        label = stringResource(R.string.conn_gps_satellites_used),
        value =
            stringResource(
                R.string.conn_gps_satellite_ratio,
                uiNumber(gps.satellitesUsed),
                uiNumber(gps.satelliteCount),
            ),
    )
}

@Composable
private fun FailedGpsRow(failure: GpsFailureCode?) {
    val classification = classifyGpsFix(GpsFixStatus.FAILED, failure)
    DataRow(
        label = stringResource(R.string.conn_gps_status),
        value =
            stringResource(
                if (failure == GpsFailureCode.START_FAILED) {
                    R.string.conn_gps_start_failed
                } else {
                    R.string.conn_gps_failed
                },
            ),
        tone = classification.toSemanticTone(),
    )
    ObservationReasonNote(classification)
}

@Composable
internal fun SatelliteSection(satellites: List<GpsSatelliteInfo>) {
    Column {
        SectionHeader(label = stringResource(R.string.conn_gps_satellite_info))
        satellites.take(MAX_VISIBLE_SATELLITES).forEach { satellite ->
            LongValueRow(
                label =
                    stringResource(
                        R.string.conn_gps_satellite_label,
                        uiNumber(satellite.svid),
                        satellite.constellation,
                    ),
                value =
                    stringResource(
                        R.string.conn_gps_satellite_value,
                        uiNumber(satellite.cn0DbHz, 1, 1),
                        if (satellite.usedInFix) {
                            stringResource(R.string.conn_gps_satellite_used_in_fix)
                        } else {
                            stringResource(R.string.conn_gps_satellite_not_used_in_fix)
                        },
                    ),
            )
        }
        if (satellites.size > MAX_VISIBLE_SATELLITES) {
            val hiddenSatelliteCount = satellites.size - MAX_VISIBLE_SATELLITES
            Note(
                text =
                    pluralStringResource(
                        R.plurals.conn_gps_more_sats,
                        hiddenSatelliteCount,
                        uiNumber(hiddenSatelliteCount),
                    ),
            )
        }
    }
}

@Composable
@Suppress("kotlin:S3776") // The section renders mutually exclusive mobile-network states.
private fun MobileNetworkDetails(
    mobile: MobileNetworkState,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val mcc = mobile.mcc
    val mnc = mobile.mnc

    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md)) {
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_phone),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        if (!mobile.isAvailable) {
            Note(text = stringResource(R.string.conn_mobile_not_supported))
            return@Column
        }

        DataRow(
            label = stringResource(R.string.conn_mobile_status),
            value = mobile.dataState?.displayName(),
        )
        LongValueRow(label = stringResource(R.string.conn_mobile_operator), value = mobile.operatorName)
        LongValueRow(label = stringResource(R.string.conn_mobile_sim_operator), value = mobile.simOperatorName)
        LongValueRow(label = stringResource(R.string.conn_mobile_phone_type), value = mobile.phoneType)
        LongValueRow(label = stringResource(R.string.conn_mobile_network_type), value = mobile.networkType)
        DataRow(
            label = stringResource(R.string.conn_mobile_signal),
            value = mobile.signalStrengthDbm?.let { stringResource(R.string.conn_dbm_format, uiNumber(it)) },
        )
        DataRow(
            label = stringResource(R.string.conn_mobile_signal_level),
            value =
                mobile.signalLevel?.let {
                    stringResource(
                        R.string.conn_signal_level_format,
                        uiNumber(it),
                        uiNumber(MAX_SIGNAL_LEVEL),
                    )
                },
        )
        DataRow(
            label = stringResource(R.string.conn_mobile_roaming),
            value =
                mobile.isRoaming?.let {
                    if (it) stringResource(R.string.status_yes) else stringResource(R.string.status_no)
                },
        )
        LongValueRow(label = stringResource(R.string.conn_mobile_cell_id), value = mobile.cellId)
        LongValueRow(
            label = stringResource(R.string.conn_mobile_mcc_mnc),
            value =
                if (mcc != null && mnc != null) {
                    stringResource(R.string.conn_mcc_mnc_format, mcc, mnc)
                } else {
                    null
                },
        )
    }
}

@Composable
private fun wifiSummary(wifi: WifiState): String =
    when {
        wifi.isConnected -> stringResource(R.string.audio_connected)
        wifi.isAvailable -> stringResource(R.string.audio_disconnected)
        else -> stringResource(R.string.conn_not_available)
    }

@Composable
private fun bluetoothSummary(
    bluetooth: BluetoothState,
    permissionState: PermissionState,
): String =
    when {
        !bluetooth.isAvailable -> stringResource(R.string.conn_not_available)
        permissionState != PermissionState.GRANTED && permissionState != PermissionState.NOT_REQUIRED ->
            stringResource(R.string.run_all_permission_missing)
        bluetooth.isEnabled == true -> stringResource(R.string.status_enabled)
        else -> stringResource(R.string.status_disabled)
    }

@Composable
private fun nfcSummary(nfc: NfcState): String =
    when {
        !nfc.isAvailable -> stringResource(R.string.conn_not_available)
        nfc.isEnabled -> stringResource(R.string.status_enabled)
        else -> stringResource(R.string.status_disabled)
    }

@Composable
private fun gpsSummary(gps: GpsState): String =
    when (gps.fixStatus) {
        GpsFixStatus.FIXED -> stringResource(R.string.conn_gps_fixed)
        GpsFixStatus.SEARCHING -> stringResource(R.string.conn_gps_searching)
        GpsFixStatus.FAILED ->
            stringResource(
                if (gps.failure == GpsFailureCode.START_FAILED) {
                    R.string.conn_gps_start_failed
                } else {
                    R.string.conn_gps_failed
                },
            )
        GpsFixStatus.NOT_STARTED ->
            when {
                !gps.isAvailable -> stringResource(R.string.conn_not_available)
                gps.isEnabled -> stringResource(R.string.status_enabled)
                else -> stringResource(R.string.status_disabled)
            }
    }

private fun GpsState.summaryTone(): SemanticTone =
    if (fixStatus == GpsFixStatus.NOT_STARTED && !isEnabled) {
        classifyGpsProvider(enabled = false).toSemanticTone()
    } else {
        classifyGpsFix(fixStatus, failure).toSemanticTone()
    }

@Composable
private fun mobileSummary(mobile: MobileNetworkState): String =
    when {
        !mobile.isAvailable -> stringResource(R.string.conn_not_available)
        mobile.isConnected -> stringResource(R.string.conn_mobile_summary_connected)
        else -> stringResource(R.string.conn_mobile_summary_disconnected)
    }

@Composable
private fun wifiFrequency(frequencyMhz: Int): String {
    val frequency = uiNumber(frequencyMhz)
    val band =
        when {
            frequencyMhz >= 5925 -> stringResource(R.string.conn_ghz_format, uiNumber(6))
            frequencyMhz >= 4900 -> stringResource(R.string.conn_ghz_format, uiNumber(5))
            frequencyMhz >= 2400 -> stringResource(R.string.conn_ghz_format, uiNumber(2.4, 1, 1))
            else -> null
        }
    return if (band == null) {
        stringResource(R.string.conn_mhz_format, frequency)
    } else {
        stringResource(R.string.conn_frequency_band_format, frequency, band)
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

private const val MAX_SIGNAL_LEVEL = 4
private const val MAX_VISIBLE_SATELLITES = 12
