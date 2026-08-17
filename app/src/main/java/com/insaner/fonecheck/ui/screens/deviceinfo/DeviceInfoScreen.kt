package com.insaner.fonecheck.ui.screens.deviceinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    onTopBarActionChange: (TopBarAction?) -> Unit = {},
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.device_clipboard_label)
    val copiedMessage = stringResource(R.string.device_copied_confirmation)
    val shareTitle = stringResource(R.string.device_share_title)
    val clipboardManager =
        remember(context) {
            checkNotNull(context.getSystemService(ClipboardManager::class.java))
        }
    val copyValue =
        remember(clipboardManager, clipboardLabel, copiedMessage, context) {
            { value: String ->
                clipboardManager.setPrimaryClip(ClipData.newPlainText(clipboardLabel, value))
                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    val snapshotText = state.info?.let { buildDeviceSnapshotText(context, it) }

    RegisterDeviceTopBarAction(
        enabled = !state.isLoading,
        onRefresh = viewModel::refresh,
        onTopBarActionChange = onTopBarActionChange,
    )

    DeviceInfoContent(
        state = state,
        onCopyValue = copyValue,
        onCopyAll = { snapshotText?.let(copyValue) },
        onExport = {
            snapshotText?.let { text ->
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                context.startActivity(Intent.createChooser(intent, shareTitle))
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun RegisterDeviceTopBarAction(
    enabled: Boolean,
    onRefresh: () -> Unit,
    onTopBarActionChange: (TopBarAction?) -> Unit,
) {
    val currentActionChange by rememberUpdatedState(onTopBarActionChange)
    val currentRefresh by rememberUpdatedState(onRefresh)
    val action =
        remember(enabled) {
            TopBarAction(
                icon = Icons.Default.Refresh,
                contentDescriptionResId = R.string.device_refresh,
                enabled = enabled,
                onClick = { currentRefresh() },
            )
        }

    SideEffect { currentActionChange(action) }
    DisposableEffect(Unit) {
        onDispose { currentActionChange(null) }
    }
}

@Composable
internal fun DeviceInfoContent(
    state: DeviceInfoState,
    onCopyValue: ((String) -> Unit)? = null,
    onCopyAll: () -> Unit = {},
    onExport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        if (state.isLoading && state.info == null) {
            Note(
                text = stringResource(R.string.device_loading),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        state.info?.let { info ->
            IdentitySection(info, onCopyValue)
            OperatingSystemSection(info, onCopyValue)
            DrmSection(info, onCopyValue)
            SecuritySection(info, onCopyValue)
        }

        state.error?.let {
            Note(
                text = stringResource(R.string.device_capture_error_description),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
            )
        }

        state.info?.let { info ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            ) {
                SecondaryButton(
                    label = stringResource(R.string.device_copy_all),
                    onClick = onCopyAll,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    label = stringResource(R.string.device_export),
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.device_captured_at, formatCapturedAt(info.capturedAt)),
                style = FonecheckTheme.type.rowValue,
                color = FonecheckTheme.colors.textMuted,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = FonecheckTheme.spacing.xs, bottom = FonecheckTheme.spacing.sm),
            )
        }
    }
}

@Composable
private fun IdentitySection(
    info: DeviceInfo,
    onCopyValue: ((String) -> Unit)?,
) {
    val model = availableDeviceValue(info.model)
    val manufacturer = availableDeviceValue(info.manufacturer)
    val brand = availableDeviceValue(info.brand)
    val product = availableDeviceValue(info.product)
    DeviceSection(label = stringResource(R.string.device_identity_title)) {
        DataRow(
            stringResource(R.string.label_model),
            model,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(model, onCopyValue),
        )
        DataRow(
            stringResource(R.string.label_manufacturer),
            manufacturer,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(manufacturer, onCopyValue),
        )
        DataRow(
            stringResource(R.string.label_brand),
            brand,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(brand, onCopyValue),
        )
        DataRow(
            stringResource(R.string.label_product),
            product,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(product, onCopyValue),
        )
        DataRow(
            label = stringResource(R.string.label_serial_number),
            value = null,
            unavailableLabel = stringResource(R.string.device_value_restricted),
            showDivider = false,
        )
        Note(stringResource(R.string.device_serial_restricted_note))
        HairlineRule()
    }
}

@Composable
private fun OperatingSystemSection(
    info: DeviceInfo,
    onCopyValue: ((String) -> Unit)?,
) {
    val androidVersion = availableDeviceValue(info.androidVersion)
    val apiLevel = info.apiLevel.toString()
    val securityPatch = availableDeviceValue(info.securityPatch)
    val buildNumber = availableDeviceValue(info.buildNumber)
    val kernelVersion = availableDeviceValue(info.kernelVersion)
    val basebandVersion = availableDeviceValue(info.basebandVersion)
    val bootloaderVersion = availableDeviceValue(info.bootloaderVersion)
    DeviceSection(label = stringResource(R.string.os_info_title)) {
        DataRow(
            stringResource(R.string.label_android_version),
            androidVersion,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(androidVersion, onCopyValue),
        )
        DataRow(
            stringResource(R.string.label_api_level),
            apiLevel,
            onValueLongClick = valueCopyAction(apiLevel, onCopyValue),
        )
        DataRow(
            stringResource(R.string.label_security_patch),
            securityPatch,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(securityPatch, onCopyValue),
        )
        DataRow(
            stringResource(R.string.label_build_number),
            buildNumber,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(buildNumber, onCopyValue),
        )
        LongValueRow(
            stringResource(R.string.label_kernel),
            splitConcatenatedDeviceIdentifiers(kernelVersion),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(kernelVersion, onCopyValue),
        )
        LongValueRow(
            stringResource(R.string.label_baseband),
            splitConcatenatedDeviceIdentifiers(basebandVersion),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(basebandVersion, onCopyValue),
        )
        LongValueRow(
            stringResource(R.string.label_bootloader),
            splitConcatenatedDeviceIdentifiers(bootloaderVersion),
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(bootloaderVersion, onCopyValue),
        )
    }
}

@Composable
private fun DrmSection(
    info: DeviceInfo,
    onCopyValue: ((String) -> Unit)?,
) {
    val widevineLevel = availableDeviceValue(info.widevineLevel)
    DeviceSection(label = stringResource(R.string.drm_info_title)) {
        DataRow(
            stringResource(R.string.label_widevine),
            widevineLevel,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(widevineLevel, onCopyValue),
        )
    }
}

@Composable
private fun SecuritySection(
    info: DeviceInfo,
    onCopyValue: ((String) -> Unit)?,
) {
    val rootStatus =
        stringResource(
            if (info.rootArtifactDetected) {
                R.string.run_all_status_warning
            } else {
                R.string.run_all_status_pass
            },
        )
    val developerOptions = enabledLabel(info.developerOptionsEnabled)
    val usbDebugging = enabledLabel(info.usbDebuggingEnabled)
    DeviceSection(label = stringResource(R.string.security_info_title)) {
        DataRow(
            label = stringResource(R.string.label_root_artifact),
            value = rootStatus,
            tone = if (info.rootArtifactDetected) SemanticTone.ATTENTION else SemanticTone.PASS,
            showDivider = false,
            onValueLongClick = valueCopyAction(rootStatus, onCopyValue),
        )
        if (info.rootArtifactDetected) {
            Note(stringResource(R.string.device_root_finding_note))
        }
        Note(stringResource(R.string.device_root_heuristic_disclaimer))
        HairlineRule()

        DataRow(
            label = stringResource(R.string.label_developer_options),
            value = developerOptions,
            showDivider = false,
            onValueLongClick = valueCopyAction(developerOptions, onCopyValue),
        )
        Note(stringResource(R.string.device_developer_options_note))
        HairlineRule()

        DataRow(
            label = stringResource(R.string.label_usb_debugging),
            value = usbDebugging,
            showDivider = false,
            onValueLongClick = valueCopyAction(usbDebugging, onCopyValue),
        )
        Note(stringResource(R.string.device_usb_debugging_note))
        HairlineRule()
    }
}

@Composable
private fun DeviceSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label)
        content()
    }
}

@Composable
private fun enabledLabel(enabled: Boolean): String =
    stringResource(if (enabled) R.string.status_enabled else R.string.status_disabled)

private fun valueCopyAction(
    value: String?,
    onCopyValue: ((String) -> Unit)?,
): (() -> Unit)? =
    if (value == null || onCopyValue == null) {
        null
    } else {
        { onCopyValue(value) }
    }

internal fun availableDeviceValue(value: String): String? = value.takeUnless { it == DeviceInfo.UNAVAILABLE }

internal fun splitConcatenatedDeviceIdentifiers(value: String?): String? {
    if (value == null) return null
    listOf(',', ';').forEach { separator ->
        val parts = value.split(separator)
        if (parts.size == 2 && parts.all { it.isNotBlank() }) {
            return "${parts[0].trimEnd()}\n${parts[1].trimStart()}"
        }
    }
    return value
}

private val capturedAtFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

internal fun formatCapturedAt(
    value: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = capturedAtFormatter.withZone(zoneId).format(value)
