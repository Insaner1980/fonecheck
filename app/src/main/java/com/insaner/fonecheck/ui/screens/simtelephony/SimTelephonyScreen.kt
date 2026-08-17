package com.insaner.fonecheck.ui.screens.simtelephony

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun SimTelephonyScreen(
    modifier: Modifier = Modifier,
    onTopBarActionChange: (TopBarAction?) -> Unit = {},
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
    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.sim_refresh,
        enabled = !state.isLoading,
        onRefresh = viewModel::refresh,
        onTopBarActionChange = onTopBarActionChange,
    )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        PermissionStatusCard(
            state = phonePermission.state,
            rationale = stringResource(R.string.permission_rationale_phone),
            onRequest = requestPhonePermission,
            onOpenSettings = phonePermission::openSettings,
        )

        if (state.isLoading && state.info == null) {
            Column {
                IndeterminateRule()
                Note(
                    text = stringResource(R.string.sim_loading),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }

        state.info?.let { info ->
            SimSection(label = stringResource(R.string.sim_telephony_title)) {
                DataRow(
                    label = stringResource(R.string.sim_inventory_label),
                    value = inventoryLabel(info.inventory),
                    tone = inventoryTone(info.inventory),
                )
                DataRow(
                    label = stringResource(R.string.label_phone_type),
                    value = phoneTypeLabel(info.phoneType),
                )
                DataRow(
                    label = stringResource(R.string.label_phone_count),
                    value = uiNumber(info.phoneCount),
                )
                DataRow(
                    label = stringResource(R.string.label_data_network),
                    value = networkLabel(info.dataNetworkType),
                    showDivider = info.phoneStatePermissionGranted || !hasTelephony,
                )
                if (!info.phoneStatePermissionGranted && hasTelephony) {
                    Note(stringResource(R.string.sim_limited_mode))
                    HairlineRule()
                }
            }

            info.simSlots.forEach { slot -> SimSlotSection(slot) }
        }

        state.error?.let {
            Note(
                text = stringResource(R.string.sim_capture_error_description),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
            )
        }
    }
}

@Composable
private fun SimSlotSection(slot: SimSlotInfo) {
    SimSection(
        label = stringResource(R.string.sim_slot_title, uiNumber(slot.slotIndex + 1)),
    ) {
        DataRow(
            label = stringResource(R.string.label_sim_status),
            value = slotStateLabel(slot.state),
            tone = slotStateTone(slot.state),
        )
        DataRow(
            label = stringResource(R.string.sim_activity_label),
            value = activityLabel(slot.activity),
        )
        DataRow(
            label = stringResource(R.string.sim_form_factor_label),
            value = formFactorLabel(slot.formFactor),
        )
        DataRow(
            label = stringResource(R.string.label_operator),
            value = slot.operatorName?.takeIf(String::isNotBlank),
        )
        DataRow(
            label = stringResource(R.string.label_country),
            value = slot.countryIso?.takeIf(String::isNotBlank),
        )
        DataRow(
            label = stringResource(R.string.label_network_type),
            value = networkLabel(slot.networkType),
        )
    }
}

@Composable
private fun SimSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label)
        content()
    }
}

private fun inventoryTone(value: SimInventoryCode): SemanticTone =
    when (value) {
        SimInventoryCode.SINGLE_SIM,
        SimInventoryCode.MULTIPLE_SIM,
        -> SemanticTone.PASS
        SimInventoryCode.INACTIVE_SIM -> SemanticTone.ATTENTION
        SimInventoryCode.NO_TELEPHONY,
        SimInventoryCode.NO_SIM,
        SimInventoryCode.UNKNOWN,
        -> SemanticTone.NEUTRAL
    }

private fun slotStateTone(value: SimSlotStateCode): SemanticTone =
    when (value) {
        SimSlotStateCode.READY -> SemanticTone.PASS
        SimSlotStateCode.NETWORK_LOCKED,
        SimSlotStateCode.PIN_REQUIRED,
        SimSlotStateCode.PUK_REQUIRED,
        SimSlotStateCode.NOT_READY,
        SimSlotStateCode.CARD_RESTRICTED,
        -> SemanticTone.ATTENTION
        SimSlotStateCode.PERMANENTLY_DISABLED,
        SimSlotStateCode.CARD_IO_ERROR,
        -> SemanticTone.FAIL
        SimSlotStateCode.ABSENT,
        SimSlotStateCode.UNKNOWN,
        -> SemanticTone.NEUTRAL
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
