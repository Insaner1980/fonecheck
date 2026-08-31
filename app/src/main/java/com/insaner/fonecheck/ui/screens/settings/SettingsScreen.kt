package com.insaner.fonecheck.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.startExternalActivity
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun SettingsRoute(
    onOpenLicenses: () -> Unit,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedLanguage by remember {
        mutableStateOf(AppLanguage.fromLocale(AppCompatDelegate.getApplicationLocales()[0]))
    }
    val currentOnOpenOnboarding by rememberUpdatedState(onOpenOnboarding)
    val context = LocalContext.current
    val packageInfo = remember { context.packageManager.getPackageInfo(context.packageName, 0) }
    val appVersion =
        stringResource(
            R.string.report_app_version_value,
            packageInfo.versionName.orEmpty(),
            uiNumber(PackageInfoCompat.getLongVersionCode(packageInfo)),
        )
    val externalAppUnavailable = stringResource(R.string.external_app_unavailable)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermissions()
        selectedLanguage = AppLanguage.fromLocale(AppCompatDelegate.getApplicationLocales()[0])
    }
    LaunchedEffect(state.openOnboarding) {
        if (state.openOnboarding) {
            viewModel.consumeOpenOnboarding()
            currentOnOpenOnboarding()
        }
    }
    SettingsScreen(
        state = state,
        appVersion = appVersion,
        onThemeMode = viewModel::setThemeMode,
        selectedLanguage = selectedLanguage,
        onLanguage = { language ->
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
            selectedLanguage = language
        },
        onTestWarnings = viewModel::setTestWarningsEnabled,
        onOpenAppSettings = {
            val intent =
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:${context.packageName}".toUri(),
                )
            if (!context.startExternalActivity(intent)) {
                Toast.makeText(context, externalAppUnavailable, Toast.LENGTH_SHORT).show()
            }
        },
        onDeleteAll = viewModel::deleteAllReports,
        onOpenPrivacy = {
            if (!context.startExternalActivity(Intent(Intent.ACTION_VIEW, PRIVACY_URL.toUri()))) {
                Toast.makeText(context, externalAppUnavailable, Toast.LENGTH_SHORT).show()
            }
        },
        onOpenSupport = {
            val intent = Intent(Intent.ACTION_SENDTO, SUPPORT_EMAIL.toUri())
            if (!context.startExternalActivity(intent)) {
                Toast.makeText(context, externalAppUnavailable, Toast.LENGTH_SHORT).show()
            }
        },
        onOpenLicenses = onOpenLicenses,
        onReopenOnboarding = viewModel::reopenOnboarding,
        modifier = modifier,
    )
}

@Composable
@Suppress("kotlin:S107") // Explicit settings actions avoid an untyped callback bag.
fun SettingsScreen(
    state: SettingsState,
    appVersion: String,
    onThemeMode: (AppThemeMode) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
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
    TestScreenContent(modifier = modifier) {
        state.error?.let { error ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
                    StatusText(
                        text = stringResource(R.string.state_error_title),
                        tone = SemanticTone.FAIL,
                    )
                    Note(
                        stringResource(
                            if (error == "delete_reports_failed") {
                                R.string.settings_delete_error
                            } else {
                                R.string.settings_error
                            },
                        ),
                    )
                }
            }
        }
        item { AppearanceSection(state, onThemeMode, selectedLanguage, onLanguage, onTestWarnings) }
        item { PermissionSection(state.permissions, onOpenAppSettings) }
        item {
            ReportsSection(
                reportCount = state.reportCount,
                isDeleting = state.isDeletingReports,
                onDeleteAll = { confirmDeleteAll = true },
            )
        }
        item {
            LinkSection(
                onOpenPrivacy = onOpenPrivacy,
                onOpenSupport = onOpenSupport,
                onOpenLicenses = onOpenLicenses,
                onReopenOnboarding = onReopenOnboarding,
            )
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.settings_about))
                LongValueRow(
                    label = stringResource(R.string.settings_version),
                    value = appVersion,
                )
                Note(stringResource(R.string.settings_disclaimer))
            }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_delete_all_title),
                    style = FonecheckTheme.type.screenTitle,
                    color = FonecheckTheme.colors.textPrimary,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_delete_all_message),
                    style = FonecheckTheme.type.note,
                    color = FonecheckTheme.colors.textMuted,
                )
            },
            confirmButton = {
                PrimaryButton(
                    label = stringResource(R.string.settings_delete_all_confirm),
                    onClick = {
                        confirmDeleteAll = false
                        onDeleteAll()
                    },
                    modifier = Modifier.testTag("settings_confirm_delete_all"),
                )
            },
            dismissButton = {
                SecondaryButton(
                    label = stringResource(R.string.settings_delete_all_cancel),
                    onClick = { confirmDeleteAll = false },
                )
            },
        )
    }
}

@Composable
private fun AppearanceSection(
    state: SettingsState,
    onThemeMode: (AppThemeMode) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    onTestWarnings: (Boolean) -> Unit,
) {
    Column {
        SectionHeader(stringResource(R.string.settings_appearance))
        Note(stringResource(R.string.settings_theme))
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            AppThemeMode.entries.forEach { mode ->
                SettingsChoice(
                    label =
                        stringResource(
                            when (mode) {
                                AppThemeMode.SYSTEM -> R.string.settings_theme_system
                                AppThemeMode.LIGHT -> R.string.settings_theme_light
                                AppThemeMode.DARK -> R.string.settings_theme_dark
                            },
                        ),
                    isSelected = state.preferences.themeMode == mode,
                    testTag = "settings_theme_${mode.name.lowercase()}",
                    onClick = { onThemeMode(mode) },
                )
            }
        }
        Note(
            text = stringResource(R.string.settings_language),
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            AppLanguage.entries.forEach { language ->
                SettingsChoice(
                    label =
                        stringResource(
                            when (language) {
                                AppLanguage.SYSTEM -> R.string.settings_language_system
                                AppLanguage.ENGLISH -> R.string.settings_language_english
                                AppLanguage.FINNISH -> R.string.settings_language_finnish
                            },
                        ),
                    isSelected = selectedLanguage == language,
                    testTag = "settings_language_${language.name.lowercase()}",
                    onClick = { onLanguage(language) },
                )
            }
        }
        SettingToggleRow(
            checked = state.preferences.testWarningsEnabled,
            onCheckedChange = onTestWarnings,
        )
    }
}

@Composable
private fun SettingsChoice(
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    val modifier =
        Modifier
            .testTag(testTag)
            .semantics { this.selected = isSelected }
    if (isSelected) {
        PrimaryButton(
            label = label,
            onClick = onClick,
            modifier = modifier,
        )
    } else {
        SecondaryButton(
            label = label,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun SettingToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = FonecheckTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_test_warnings),
                    style = FonecheckTheme.type.rowLabel,
                    color = FonecheckTheme.colors.textSecondary,
                )
                Text(
                    text = stringResource(R.string.settings_test_warnings_description),
                    style = FonecheckTheme.type.note,
                    color = FonecheckTheme.colors.textMuted,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag("settings_test_warnings"),
            )
        }
        HairlineRule()
    }
}

@Composable
private fun PermissionSection(
    permissions: SettingsPermissionSnapshot,
    onOpenAppSettings: () -> Unit,
) {
    Column {
        SectionHeader(stringResource(R.string.settings_permissions))
        Note(stringResource(R.string.settings_permissions_description))
        PermissionRow(R.string.settings_permission_camera, permissions.camera)
        PermissionRow(R.string.settings_permission_microphone, permissions.microphone)
        PermissionRow(R.string.settings_permission_phone, permissions.phone)
        PermissionRow(R.string.settings_permission_location, permissions.location)
        PermissionRow(R.string.settings_permission_bluetooth, permissions.bluetooth)
        SecondaryButton(
            label = stringResource(R.string.settings_open_app_settings),
            onClick = onOpenAppSettings,
            modifier = Modifier.fillMaxWidth().testTag("settings_open_app_settings"),
        )
    }
}

@Composable
private fun PermissionRow(
    label: Int,
    granted: Boolean,
) {
    DataRow(
        label = stringResource(label),
        value =
            stringResource(
                if (granted) {
                    R.string.settings_permission_granted
                } else {
                    R.string.settings_permission_not_granted
                },
            ),
        tone = if (granted) SemanticTone.PASS else SemanticTone.NEUTRAL,
    )
}

@Composable
private fun ReportsSection(
    reportCount: Int,
    isDeleting: Boolean,
    onDeleteAll: () -> Unit,
) {
    Column {
        SectionHeader(stringResource(R.string.settings_reports))
        DataRow(
            label = stringResource(R.string.settings_report_count),
            value = uiNumber(reportCount),
        )
        SecondaryButton(
            label = stringResource(R.string.settings_delete_all),
            onClick = onDeleteAll,
            enabled = reportCount > 0 && !isDeleting,
            modifier = Modifier.fillMaxWidth().testTag("settings_delete_all"),
        )
    }
}

@Composable
private fun LinkSection(
    onOpenPrivacy: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenLicenses: () -> Unit,
    onReopenOnboarding: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.settings_privacy_section))
        Note(stringResource(R.string.settings_local_only))
        SettingsLink(R.string.settings_privacy, "settings_privacy", onOpenPrivacy)
        SettingsLink(R.string.settings_support, "settings_support", onOpenSupport)
        SettingsLink(R.string.settings_licenses, "settings_licenses", onOpenLicenses)
        SettingsLink(R.string.settings_onboarding, "settings_onboarding", onReopenOnboarding)
    }
}

@Composable
private fun SettingsLink(
    label: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    SecondaryButton(
        label = stringResource(label),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    )
}

private const val PRIVACY_URL = "https://finnvek.com/privacy/"
private const val SUPPORT_EMAIL = "mailto:contact@finnvek.com"
