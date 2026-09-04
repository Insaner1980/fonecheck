package com.insaner.fonecheck.ui.permissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.insaner.fonecheck.domain.permission.AppPermission
import com.insaner.fonecheck.domain.permission.PermissionEvaluation
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionPolicy
import com.insaner.fonecheck.domain.permission.PermissionState

class PermissionController internal constructor(
    private val context: Context,
    private val activity: Activity?,
    val kind: PermissionKind,
    private val hardwareAvailable: Boolean,
) {
    private val requestHistory =
        context.getSharedPreferences(REQUEST_HISTORY_NAME, Context.MODE_PRIVATE)
    private val requiredPermissions =
        PermissionPolicy.requiredPermissions(kind, Build.VERSION.SDK_INT)

    val permissions: List<String> = requiredPermissions.map(AppPermission::manifestName)

    var state by mutableStateOf(evaluate())
        private set

    fun onRequestLaunched() {
        requestHistory.edit { putBoolean(kind.name, true) }
        refresh()
    }

    fun refresh() {
        state = evaluate()
    }

    fun openSettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun evaluate(): PermissionState {
        val grantedPermissions =
            requiredPermissions.filterTo(mutableSetOf()) { permission ->
                ContextCompat.checkSelfPermission(context, permission.manifestName) ==
                    PackageManager.PERMISSION_GRANTED
            }
        val shouldShowRationale =
            requiredPermissions.any { permission ->
                permission !in grantedPermissions &&
                    activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, permission.manifestName)
                    } == true
            }
        return PermissionPolicy.evaluate(
            kind = kind,
            input =
                PermissionEvaluation(
                    sdkInt = Build.VERSION.SDK_INT,
                    hardwareAvailable = hardwareAvailable,
                    hasRequested = requestHistory.getBoolean(kind.name, false),
                    shouldShowRationale = shouldShowRationale,
                    grantedPermissions = grantedPermissions,
                ),
        )
    }

    private companion object {
        const val REQUEST_HISTORY_NAME = "permission_request_history"
    }
}

@Composable
fun rememberPermissionController(
    kind: PermissionKind,
    hardwareAvailable: Boolean = true,
): PermissionController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller =
        remember(context, kind, hardwareAvailable) {
            PermissionController(
                context = context.applicationContext,
                activity = context.findActivity(),
                kind = kind,
                hardwareAvailable = hardwareAvailable,
            )
        }

    DisposableEffect(lifecycleOwner, controller) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) controller.refresh()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return controller
}

private val AppPermission.manifestName: String
    get() =
        when (this) {
            AppPermission.MICROPHONE -> android.Manifest.permission.RECORD_AUDIO
            AppPermission.CAMERA -> android.Manifest.permission.CAMERA
            AppPermission.COARSE_LOCATION -> android.Manifest.permission.ACCESS_COARSE_LOCATION
            AppPermission.FINE_LOCATION -> android.Manifest.permission.ACCESS_FINE_LOCATION
            AppPermission.PHONE -> android.Manifest.permission.READ_PHONE_STATE
            AppPermission.BLUETOOTH_CONNECT -> "android.permission.BLUETOOTH_CONNECT"
            AppPermission.ACTIVITY_RECOGNITION -> "android.permission.ACTIVITY_RECOGNITION"
        }

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
