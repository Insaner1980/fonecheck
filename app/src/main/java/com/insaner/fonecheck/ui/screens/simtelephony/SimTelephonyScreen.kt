package com.insaner.fonecheck.ui.screens.simtelephony

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.StatusRow
import com.insaner.fonecheck.ui.permissions.rememberPermissionController

@Composable
fun SimTelephonyScreen(
    modifier: Modifier = Modifier,
    viewModel: SimTelephonyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hasTelephony =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        }
    val phonePermission =
        rememberPermissionController(
            kind = PermissionKind.PHONE,
            hardwareAvailable = hasTelephony,
        )
    val phonePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            phonePermission.refresh()
            viewModel.refresh()
        }
    val requestPhonePermission = {
        phonePermission.onRequestLaunched()
        phonePermissionLauncher.launch(phonePermission.permissions.toTypedArray())
    }

    LaunchedEffect(phonePermission.state) {
        viewModel.refresh()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PermissionStatusCard(
            state = phonePermission.state,
            rationale = stringResource(R.string.permission_rationale_phone),
            onRequest = requestPhonePermission,
            onOpenSettings = phonePermission::openSettings,
        )

        if (state.isLoading && state.info == null) {
            ScreenStateCard(
                type = ScreenStateType.LOADING,
                message = stringResource(R.string.sim_loading),
            )
        }

        state.info?.let { info ->
            InfoCard(title = stringResource(R.string.sim_telephony_title)) {
                StatusRow(
                    label = stringResource(R.string.sim_inventory_label),
                    value = inventoryLabel(info.inventory),
                    isHighlighted =
                        info.inventory != SimInventoryCode.SINGLE_SIM &&
                            info.inventory != SimInventoryCode.MULTIPLE_SIM,
                )
                InfoRow(stringResource(R.string.label_phone_type), phoneTypeLabel(info.phoneType))
                InfoRow(stringResource(R.string.label_phone_count), info.phoneCount.toString())
                InfoRow(stringResource(R.string.label_data_network), networkLabel(info.dataNetworkType))
                if (!info.phoneStatePermissionGranted && hasTelephony) {
                    Text(
                        text = stringResource(R.string.sim_limited_mode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            info.simSlots.forEach { slot -> SimSlotCard(slot) }
        }

        state.error?.let {
            ScreenStateCard(
                type = ScreenStateType.ERROR,
                message = stringResource(R.string.sim_capture_error_description),
                actionLabel = stringResource(R.string.sim_refresh),
                onAction = viewModel::refresh,
            )
        }

        if (state.error == null) {
            Button(
                onClick = viewModel::refresh,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.sim_refresh))
            }
        }
    }
}

@Composable
private fun SimSlotCard(slot: SimSlotInfo) {
    InfoCard(title = stringResource(R.string.sim_slot_title, slot.slotIndex + 1)) {
        StatusRow(
            label = stringResource(R.string.label_sim_status),
            value = slotStateLabel(slot.state),
            isHighlighted = slot.state != SimSlotStateCode.READY && slot.state != SimSlotStateCode.ABSENT,
        )
        InfoRow(stringResource(R.string.sim_activity_label), activityLabel(slot.activity))
        InfoRow(stringResource(R.string.sim_form_factor_label), formFactorLabel(slot.formFactor))
        InfoRow(stringResource(R.string.label_operator), slot.operatorName ?: unavailableLabel())
        InfoRow(stringResource(R.string.label_country), slot.countryIso ?: unavailableLabel())
        InfoRow(stringResource(R.string.label_network_type), networkLabel(slot.networkType))
    }
}

@Composable
private fun inventoryLabel(value: SimInventoryCode): String =
    stringResource(
        when (value) {
            SimInventoryCode.NO_TELEPHONY -> R.string.sim_inventory_no_telephony
            SimInventoryCode.NO_SIM -> R.string.sim_inventory_no_sim
            SimInventoryCode.INACTIVE_SIM -> R.string.sim_inventory_inactive
            SimInventoryCode.SINGLE_SIM -> R.string.sim_inventory_single
            SimInventoryCode.MULTIPLE_SIM -> R.string.sim_inventory_multiple
            SimInventoryCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun slotStateLabel(value: SimSlotStateCode): String =
    stringResource(
        when (value) {
            SimSlotStateCode.READY -> R.string.sim_state_ready
            SimSlotStateCode.ABSENT -> R.string.sim_state_absent
            SimSlotStateCode.NETWORK_LOCKED -> R.string.sim_state_network_locked
            SimSlotStateCode.PIN_REQUIRED -> R.string.sim_state_pin_required
            SimSlotStateCode.PUK_REQUIRED -> R.string.sim_state_puk_required
            SimSlotStateCode.NOT_READY -> R.string.sim_state_not_ready
            SimSlotStateCode.PERMANENTLY_DISABLED -> R.string.sim_state_permanently_disabled
            SimSlotStateCode.CARD_IO_ERROR -> R.string.sim_state_card_io_error
            SimSlotStateCode.CARD_RESTRICTED -> R.string.sim_state_card_restricted
            SimSlotStateCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun activityLabel(value: SimActivityCode): String =
    stringResource(
        when (value) {
            SimActivityCode.ACTIVE -> R.string.sim_activity_active
            SimActivityCode.INACTIVE -> R.string.sim_activity_inactive
            SimActivityCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun formFactorLabel(value: SimFormFactorCode): String =
    stringResource(
        when (value) {
            SimFormFactorCode.EMBEDDED -> R.string.sim_form_factor_embedded
            SimFormFactorCode.PHYSICAL -> R.string.sim_form_factor_physical
            SimFormFactorCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun phoneTypeLabel(value: PhoneTypeCode): String =
    stringResource(
        when (value) {
            PhoneTypeCode.GSM -> R.string.sim_phone_type_gsm
            PhoneTypeCode.CDMA -> R.string.sim_phone_type_cdma
            PhoneTypeCode.SIP -> R.string.sim_phone_type_sip
            PhoneTypeCode.NONE -> R.string.sim_phone_type_none
            PhoneTypeCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun networkLabel(value: NetworkGenerationCode): String =
    stringResource(
        when (value) {
            NetworkGenerationCode.SECOND_GENERATION -> R.string.sim_network_2g
            NetworkGenerationCode.THIRD_GENERATION -> R.string.sim_network_3g
            NetworkGenerationCode.FOURTH_GENERATION -> R.string.sim_network_4g
            NetworkGenerationCode.FIFTH_GENERATION -> R.string.sim_network_5g
            NetworkGenerationCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun unavailableLabel(): String = stringResource(R.string.device_value_unavailable)
