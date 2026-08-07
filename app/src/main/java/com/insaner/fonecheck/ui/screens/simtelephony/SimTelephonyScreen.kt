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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.StatusRow
import com.insaner.fonecheck.ui.permissions.rememberPermissionController

@Composable
fun SimTelephonyScreen(
    modifier: Modifier = Modifier,
    viewModel: SimTelephonyViewModel = hiltViewModel(),
) {
    val info by viewModel.simTelephonyInfo.collectAsStateWithLifecycle()
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
        if (phonePermission.state == PermissionState.GRANTED) {
            viewModel.refresh()
        }
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

        // Telephony Overview Card
        InfoCard(title = stringResource(R.string.sim_telephony_title)) {
            InfoRow(stringResource(R.string.label_phone_type), info.phoneType)
            InfoRow(stringResource(R.string.label_phone_count), info.phoneCount.toString())
            StatusRow(
                label = stringResource(R.string.label_dual_sim),
                value = if (info.isDualSim) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
                isHighlighted = info.isDualSim,
            )
            InfoRow(stringResource(R.string.label_data_network), info.dataNetworkType)
        }

        // SIM Slot Cards
        info.simSlots.forEach { slot ->
            SimSlotCard(slot)
        }
    }
}

@Composable
private fun SimSlotCard(slot: SimSlotInfo) {
    val isPresent = slot.status == "Present"
    InfoCard(title = stringResource(R.string.sim_slot_title, slot.slotIndex + 1)) {
        StatusRow(
            label = stringResource(R.string.label_sim_status),
            value = slot.status,
            isHighlighted = !isPresent,
        )
        InfoRow(stringResource(R.string.label_operator), slot.operatorName)
        InfoRow(stringResource(R.string.label_country), slot.countryIso)
        InfoRow(stringResource(R.string.label_network_type), slot.networkType)
    }
}
