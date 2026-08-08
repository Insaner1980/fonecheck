package com.insaner.fonecheck.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.theme.Green400

@Composable
fun SettingsRoute(
    onOpenLicenses: () -> Unit,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val packageInfo = remember { context.packageManager.getPackageInfo(context.packageName, 0) }
    val appVersion = "${packageInfo.versionName} (${PackageInfoCompat.getLongVersionCode(packageInfo)})"
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }
    LaunchedEffect(state.openOnboarding) {
        if (state.openOnboarding) {
            viewModel.consumeOpenOnboarding()
            onOpenOnboarding()
        }
    }
    SettingsScreen(
        state = state,
        appVersion = appVersion,
        onThemeMode = viewModel::setThemeMode,
        onTestWarnings = viewModel::setTestWarningsEnabled,
        onOpenAppSettings = {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
        },
        onDeleteAll = viewModel::deleteAllReports,
        onOpenPrivacy = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL)))
        },
        onOpenSupport = {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(SUPPORT_EMAIL)))
        },
        onOpenLicenses = onOpenLicenses,
        onReopenOnboarding = viewModel::reopenOnboarding,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    appVersion: String,
    onThemeMode: (AppThemeMode) -> Unit,
    onTestWarnings: (Boolean) -> Unit,
    onOpenAppSettings: () -> Unit,
    onDeleteAll: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenLicenses: () -> Unit,
    onReopenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDeleteAll by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        state.error?.let { error ->
            item {
                Text(
                    text =
                        stringResource(
                            if (error == "delete_reports_failed") {
                                R.string.settings_delete_error
                            } else {
                                R.string.settings_error
                            },
                        ),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item { AppearanceCard(state, onThemeMode, onTestWarnings) }
        item { PermissionCard(state.permissions, onOpenAppSettings) }
        item {
            ReportsCard(
                reportCount = state.reportCount,
                isDeleting = state.isDeletingReports,
                onDeleteAll = { confirmDeleteAll = true },
            )
        }
        item {
            LinkCard(
                onOpenPrivacy = onOpenPrivacy,
                onOpenSupport = onOpenSupport,
                onOpenLicenses = onOpenLicenses,
                onReopenOnboarding = onReopenOnboarding,
            )
        }
        item {
            StandardCard {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_about),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    InfoRow(stringResource(R.string.settings_version), appVersion)
                    Text(
                        text = stringResource(R.string.settings_disclaimer),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.settings_delete_all_title)) },
            text = { Text(stringResource(R.string.settings_delete_all_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmDeleteAll = false
                        onDeleteAll()
                    },
                    modifier = Modifier.testTag("settings_confirm_delete_all"),
                ) {
                    Text(stringResource(R.string.settings_delete_all_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text(stringResource(R.string.settings_delete_all_cancel))
                }
            },
        )
    }
}

@Composable
private fun AppearanceCard(
    state: SettingsState,
    onThemeMode: (AppThemeMode) -> Unit,
    onTestWarnings: (Boolean) -> Unit,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(stringResource(R.string.settings_theme))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(AppThemeMode.SYSTEM, state.preferences.themeMode, onThemeMode)
                ThemeChip(AppThemeMode.LIGHT, state.preferences.themeMode, onThemeMode)
                ThemeChip(AppThemeMode.DARK, state.preferences.themeMode, onThemeMode)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_test_warnings))
                    Text(
                        text = stringResource(R.string.settings_test_warnings_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.preferences.testWarningsEnabled,
                    onCheckedChange = onTestWarnings,
                    modifier = Modifier.testTag("settings_test_warnings"),
                )
            }
        }
    }
}

@Composable
private fun ThemeChip(
    mode: AppThemeMode,
    selected: AppThemeMode,
    onThemeMode: (AppThemeMode) -> Unit,
) {
    val label =
        stringResource(
            when (mode) {
                AppThemeMode.SYSTEM -> R.string.settings_theme_system
                AppThemeMode.LIGHT -> R.string.settings_theme_light
                AppThemeMode.DARK -> R.string.settings_theme_dark
            },
        )
    FilterChip(
        selected = selected == mode,
        onClick = { onThemeMode(mode) },
        label = { Text(label) },
        modifier = Modifier.testTag("settings_theme_${mode.name.lowercase()}"),
    )
}

@Composable
private fun PermissionCard(
    permissions: SettingsPermissionSnapshot,
    onOpenAppSettings: () -> Unit,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_permissions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.settings_permissions_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionBox {
                PermissionRow(R.string.settings_permission_camera, permissions.camera)
                PermissionRow(R.string.settings_permission_microphone, permissions.microphone)
                PermissionRow(R.string.settings_permission_phone, permissions.phone)
                PermissionRow(R.string.settings_permission_location, permissions.location)
                PermissionRow(R.string.settings_permission_bluetooth, permissions.bluetooth)
            }
            OutlinedButton(
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth().testTag("settings_open_app_settings"),
            ) {
                Text(stringResource(R.string.settings_open_app_settings))
            }
        }
    }
}

@Composable
private fun PermissionRow(
    label: Int,
    granted: Boolean,
) {
    InfoRow(
        label = stringResource(label),
        value =
            stringResource(
                if (granted) {
                    R.string.settings_permission_granted
                } else {
                    R.string.settings_permission_not_granted
                },
            ),
        valueColor = if (granted) Green400 else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ReportsCard(
    reportCount: Int,
    isDeleting: Boolean,
    onDeleteAll: () -> Unit,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_reports),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            InfoRow(stringResource(R.string.settings_report_count), reportCount.toString())
            OutlinedButton(
                onClick = onDeleteAll,
                enabled = reportCount > 0 && !isDeleting,
                modifier = Modifier.fillMaxWidth().testTag("settings_delete_all"),
            ) {
                Text(stringResource(R.string.settings_delete_all))
            }
        }
    }
}

@Composable
private fun LinkCard(
    onOpenPrivacy: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenLicenses: () -> Unit,
    onReopenOnboarding: () -> Unit,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_privacy_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.settings_local_only),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingsLink(R.string.settings_privacy, "settings_privacy", onOpenPrivacy)
            SettingsLink(R.string.settings_support, "settings_support", onOpenSupport)
            SettingsLink(R.string.settings_licenses, "settings_licenses", onOpenLicenses)
            SettingsLink(R.string.settings_onboarding, "settings_onboarding", onReopenOnboarding)
        }
    }
}

@Composable
private fun SettingsLink(
    label: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    ) {
        Text(stringResource(label))
    }
}

private const val PRIVACY_URL = "https://finnvek.com/privacy/"
private const val SUPPORT_EMAIL = "mailto:contact@finnvek.com"
