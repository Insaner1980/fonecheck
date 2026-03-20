package com.insaner.phonecheck.ui.screens.performance

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.phonecheck.R
import com.insaner.phonecheck.domain.model.Confidence
import com.insaner.phonecheck.domain.model.CpuCoreFrequency
import com.insaner.phonecheck.ui.components.ConfidenceBadge
import com.insaner.phonecheck.ui.theme.JetBrainsMono
import com.insaner.phonecheck.ui.theme.Neutral600

@Composable
fun PerformanceInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: PerformanceInfoViewModel = hiltViewModel(),
) {
    val info = viewModel.performanceInfo

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // CPU Card
        InfoCard(
            title = stringResource(R.string.perf_cpu_title),
            confidence = info.cpuConfidence,
        ) {
            InfoRow(stringResource(R.string.perf_cpu_model), info.cpuModel)
            InfoRow(stringResource(R.string.perf_cpu_architecture), info.cpuArchitecture)
            InfoRow(stringResource(R.string.perf_cpu_cores), info.cpuCores.toString())
            if (info.cpuFrequencies.isNotEmpty()) {
                HorizontalDivider(
                    color = Neutral600,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Text(
                    text = stringResource(R.string.perf_cpu_clock_speeds),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                info.cpuFrequencies.forEach { freq ->
                    CoreFrequencyRow(freq)
                }
            }
        }

        // RAM Card
        InfoCard(
            title = stringResource(R.string.perf_ram_title),
            confidence = info.ramConfidence,
        ) {
            InfoRow(stringResource(R.string.perf_ram_total), info.totalRam)
            InfoRow(stringResource(R.string.perf_ram_available), info.availableRam)
        }

        // GPU Card
        InfoCard(
            title = stringResource(R.string.perf_gpu_title),
            confidence = info.gpuConfidence,
        ) {
            InfoRow(stringResource(R.string.perf_gpu_gles_version), info.glEsVersion)
            InfoRow(stringResource(R.string.perf_gpu_renderer), info.glRenderer)
            InfoRow(stringResource(R.string.perf_gpu_vendor), info.glVendor)
            InfoRow(
                stringResource(R.string.perf_gpu_vulkan),
                if (info.vulkanSupported) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    confidence: Confidence,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                ConfidenceBadge(confidence = confidence)
            }
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
private fun CoreFrequencyRow(freq: CpuCoreFrequency) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.perf_cpu_core_label, freq.coreIndex),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(
                R.string.perf_cpu_freq_range,
                freq.currentMhz,
                freq.minMhz,
                freq.maxMhz,
            ),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}
