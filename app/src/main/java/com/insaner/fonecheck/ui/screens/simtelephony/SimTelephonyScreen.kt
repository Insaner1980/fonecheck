package com.insaner.fonecheck.ui.screens.simtelephony

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.StatusRow

@Composable
fun SimTelephonyScreen(
    modifier: Modifier = Modifier,
    viewModel: SimTelephonyViewModel = hiltViewModel(),
) {
    val info by viewModel.simTelephonyInfo.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
