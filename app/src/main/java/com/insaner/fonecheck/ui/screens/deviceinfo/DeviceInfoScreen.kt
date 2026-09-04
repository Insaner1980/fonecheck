package com.insaner.fonecheck.ui.screens.deviceinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.localization.observationStatusStringRes
import com.insaner.fonecheck.ui.TopBarActionRegistry
import com.insaner.fonecheck.ui.components.ButtonRow
import com.insaner.fonecheck.ui.components.CaptureTimestamp
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.formatCaptureTimestamp
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.toSemanticTone
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    topBarActionRegistry: TopBarActionRegistry = TopBarActionRegistry.NoOp,
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
        topBarActionRegistry = topBarActionRegistry,
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
    topBarActionRegistry: TopBarActionRegistry,
) {
    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.device_refresh,
        enabled = enabled,
        onRefresh = onRefresh,
        topBarActionRegistry = topBarActionRegistry,
    )
}

@Composable
internal fun DeviceInfoContent(
    state: DeviceInfoState,
    modifier: Modifier = Modifier,
    onCopyValue: ((String) -> Unit)? = null,
    onCopyAll: () -> Unit = {},
    onExport: () -> Unit = {},
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
            ButtonRow { buttonModifier ->
                SecondaryButton(
                    label = stringResource(R.string.device_copy_all),
                    onClick = onCopyAll,
                    modifier = buttonModifier,
                )
                PrimaryButton(
                    label = stringResource(R.string.device_export),
                    onClick = onExport,
                    modifier = buttonModifier,
                )
            }
            CaptureTimestamp(info.capturedAt)
        }
    }
}

@Composable
private fun IdentitySection(
    info: DeviceInfo,
    onCopyValue: ((String) -> Unit)?,
) {
    val serialClassification =
        DeviceObservationClassifier.classify(DeviceObservation.SerialNumber(available = false))
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
        ObservationReasonNote(serialClassification)
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
    val kernelVersion = splitConcatenatedDeviceIdentifiers(availableDeviceValue(info.kernelVersion))
    val basebandVersion = splitConcatenatedDeviceIdentifiers(availableDeviceValue(info.basebandVersion))
    val bootloaderVersion = splitConcatenatedDeviceIdentifiers(availableDeviceValue(info.bootloaderVersion))
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
            kernelVersion,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(kernelVersion, onCopyValue),
        )
        LongValueRow(
            stringResource(R.string.label_baseband),
            basebandVersion,
            unavailableLabel = stringResource(R.string.device_value_unavailable),
            onValueLongClick = valueCopyAction(basebandVersion, onCopyValue),
        )
        LongValueRow(
            stringResource(R.string.label_bootloader),
            bootloaderVersion,
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
    val rootClassification =
        DeviceObservationClassifier.classify(DeviceObservation.RootArtifact(info.rootArtifactDetected))
    val developerOptionsClassification =
        DeviceObservationClassifier.classify(DeviceObservation.DeveloperOptions(info.developerOptionsEnabled))
    val usbDebuggingClassification =
        DeviceObservationClassifier.classify(DeviceObservation.UsbDebugging(info.usbDebuggingEnabled))
    val rootStatus =
        stringResource(observationStatusStringRes(rootClassification))
    val developerOptions = enabledLabel(info.developerOptionsEnabled)
    val usbDebugging = enabledLabel(info.usbDebuggingEnabled)
    DeviceSection(label = stringResource(R.string.security_info_title)) {
        DataRow(
            label = stringResource(R.string.label_root_artifact),
            value = rootStatus,
            tone = rootClassification.toSemanticTone(),
            showDivider = false,
            onValueLongClick = valueCopyAction(rootStatus, onCopyValue),
        )
        ObservationReasonNote(rootClassification)
        Note(stringResource(R.string.device_root_heuristic_disclaimer))
        HairlineRule()

        DataRow(
            label = stringResource(R.string.label_developer_options),
            value = developerOptions,
            showDivider = false,
            onValueLongClick = valueCopyAction(developerOptions, onCopyValue),
        )
        if (developerOptionsClassification.reason != null) {
            ObservationReasonNote(developerOptionsClassification)
        } else {
            Note(stringResource(R.string.device_developer_options_note))
        }
        HairlineRule()

        DataRow(
            label = stringResource(R.string.label_usb_debugging),
            value = usbDebugging,
            showDivider = false,
            onValueLongClick = valueCopyAction(usbDebugging, onCopyValue),
        )
        if (usbDebuggingClassification.reason != null) {
            ObservationReasonNote(usbDebuggingClassification)
        } else {
            Note(stringResource(R.string.device_usb_debugging_note))
        }
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
    val parts = value.split(',', ';')
    return if (parts.size == 2 && parts.all { it.isNotBlank() }) {
        parts.map(String::trim).distinct().joinToString("\n")
    } else {
        value
    }
}

internal fun formatCapturedAt(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = formatCaptureTimestamp(value, locale, zoneId)
