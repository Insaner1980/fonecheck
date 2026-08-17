package com.insaner.fonecheck.ui.screens.deviceinfo

import android.content.Context
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import java.time.ZoneId

internal fun buildDeviceSnapshotText(
    context: Context,
    info: DeviceInfo,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String =
    buildString {
        appendLine(context.getString(R.string.device_info_title))

        appendSection(context.getString(R.string.device_identity_title))
        appendRow(context.getString(R.string.label_model), displayValue(context, info.model))
        appendRow(context.getString(R.string.label_manufacturer), displayValue(context, info.manufacturer))
        appendRow(context.getString(R.string.label_brand), displayValue(context, info.brand))
        appendRow(context.getString(R.string.label_product), displayValue(context, info.product))
        appendRow(
            context.getString(R.string.label_serial_number),
            context.getString(R.string.device_value_restricted),
        )
        appendLine(context.getString(R.string.device_serial_restricted_note))

        appendSection(context.getString(R.string.os_info_title))
        appendRow(
            context.getString(R.string.label_android_version),
            displayValue(context, info.androidVersion),
        )
        appendRow(context.getString(R.string.label_api_level), info.apiLevel.toString())
        appendRow(context.getString(R.string.label_security_patch), displayValue(context, info.securityPatch))
        appendRow(context.getString(R.string.label_build_number), displayValue(context, info.buildNumber))
        appendRow(
            context.getString(R.string.label_kernel),
            displayLongValue(context, info.kernelVersion),
        )
        appendRow(
            context.getString(R.string.label_baseband),
            displayLongValue(context, info.basebandVersion),
        )
        appendRow(
            context.getString(R.string.label_bootloader),
            displayLongValue(context, info.bootloaderVersion),
        )

        appendSection(context.getString(R.string.drm_info_title))
        appendRow(context.getString(R.string.label_widevine), displayValue(context, info.widevineLevel))

        appendSection(context.getString(R.string.security_info_title))
        appendRow(
            context.getString(R.string.label_root_artifact),
            context.getString(
                if (info.rootArtifactDetected) {
                    R.string.run_all_status_warning
                } else {
                    R.string.run_all_status_pass
                },
            ),
        )
        if (info.rootArtifactDetected) {
            appendLine(context.getString(R.string.device_root_finding_note))
        }
        appendLine(context.getString(R.string.device_root_heuristic_disclaimer))
        appendRow(
            context.getString(R.string.label_developer_options),
            context.getString(
                if (info.developerOptionsEnabled) R.string.status_enabled else R.string.status_disabled,
            ),
        )
        appendLine(context.getString(R.string.device_developer_options_note))
        appendRow(
            context.getString(R.string.label_usb_debugging),
            context.getString(
                if (info.usbDebuggingEnabled) R.string.status_enabled else R.string.status_disabled,
            ),
        )
        appendLine(context.getString(R.string.device_usb_debugging_note))

        appendLine()
        append(context.getString(R.string.device_captured_at, formatCapturedAt(info.capturedAt, zoneId)))
    }

private fun StringBuilder.appendSection(title: String) {
    appendLine()
    appendLine(title)
}

private fun StringBuilder.appendRow(
    label: String,
    value: String,
) {
    append(label)
    append(": ")
    appendLine(value)
}

private fun displayValue(
    context: Context,
    value: String,
): String = availableDeviceValue(value) ?: context.getString(R.string.device_value_unavailable)

private fun displayLongValue(
    context: Context,
    value: String,
): String =
    splitConcatenatedDeviceIdentifiers(availableDeviceValue(value))
        ?: context.getString(R.string.device_value_unavailable)
