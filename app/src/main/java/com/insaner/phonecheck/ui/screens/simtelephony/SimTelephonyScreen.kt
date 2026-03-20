package com.insaner.phonecheck.ui.screens.simtelephony

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.phonecheck.R
import com.insaner.phonecheck.domain.model.SimSlotInfo
import com.insaner.phonecheck.ui.theme.Green400
import com.insaner.phonecheck.ui.theme.JetBrainsMono
import com.insaner.phonecheck.ui.theme.Red400
import com.insaner.phonecheck.ui.theme.Yellow400

@Composable
fun SimTelephonyScreen(
    modifier: Modifier = Modifier,
    viewModel: SimTelephonyViewModel = hiltViewModel(),
) {
    val info = viewModel.simTelephonyInfo

    Column(
        modifier = modifier
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

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String, isHighlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
            ),
            color = if (isHighlighted) Yellow400 else Green400,
        )
    }
}
