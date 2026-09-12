package com.insaner.fonecheck.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.components.ScreenStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.screens.audio.AudioTestScreen
import com.insaner.fonecheck.ui.screens.battery.BatteryTestScreen
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestScreen
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestScreen
import com.insaner.fonecheck.ui.screens.camera.CameraTestScreen
import com.insaner.fonecheck.ui.screens.comparison.ReportComparisonRoute
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestScreen
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoScreen
import com.insaner.fonecheck.ui.screens.display.DisplayTestScreen
import com.insaner.fonecheck.ui.screens.export.ReportExportRoute
import com.insaner.fonecheck.ui.screens.history.HistoryRoute
import com.insaner.fonecheck.ui.screens.home.HomeScreen
import com.insaner.fonecheck.ui.screens.onboarding.OnboardingRoute
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoScreen
import com.insaner.fonecheck.ui.screens.report.ReportDetailRoute
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsScreen
import com.insaner.fonecheck.ui.screens.sensor.SensorTestScreen
import com.insaner.fonecheck.ui.screens.settings.LanguageSelectionRoute
import com.insaner.fonecheck.ui.screens.settings.LicensesScreen
import com.insaner.fonecheck.ui.screens.settings.SettingsRoute
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyScreen
import com.insaner.fonecheck.ui.screens.storage.StorageTestScreen
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestScreen
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestScreen

@Composable
fun FonecheckNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onDisplayFullscreenChange: (Boolean) -> Unit = {},
    onTopBarActionChange: (TopBarAction?) -> Unit = {},
    appPreferences: AppPreferences = AppPreferences(),
) {
    fun publishTopBarAction(
        source: NavBackStackEntry,
        action: TopBarAction?,
    ) {
        if (navController.currentBackStackEntry === source) onTopBarActionChange(action)
    }

    NavHost(
        navController = navController,
        startDestination = initialDestination(appPreferences),
        modifier = modifier,
    ) {
        composable<Home> { backStackEntry ->
            HomeScreen(
                onNavigate = { route -> navController.navigateFrom(backStackEntry, route) },
                onRunAllTests = { navController.navigateFrom(backStackEntry, RunAllTests) },
            )
        }
        composable<DeviceInfo> { backStackEntry ->
            DeviceInfoScreen(onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) })
        }
        composable<PerformanceInfo> { backStackEntry ->
            PerformanceInfoScreen(onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) })
        }
        composable<SimTelephony> { backStackEntry ->
            SimTelephonyScreen(onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) })
        }
        composable<AudioTest> {
            AudioTestScreen()
        }
        composable<CameraTest> {
            CameraTestScreen()
        }
        composable<SensorTest> {
            SensorTestScreen()
        }
        composable<ConnectivityTest> { backStackEntry ->
            ConnectivityTestScreen(onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) })
        }
        composable<BatteryTest> {
            BatteryTestScreen()
        }
        composable<ThermalTest> { backStackEntry ->
            ThermalTestScreen(onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) })
        }
        composable<StorageTest> { backStackEntry ->
            StorageTestScreen(onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) })
        }
        composable<DisplayTest> { backStackEntry ->
            DisplayTestScreen(
                onFullscreenChange = onDisplayFullscreenChange,
                onTopBarActionChange = { action -> publishTopBarAction(backStackEntry, action) },
            )
        }
        composable<VibrationTest> {
            VibrationTestScreen()
        }
        composable<ButtonTest> {
            ButtonTestScreen()
        }
        composable<BiometricTest> {
            BiometricTestScreen()
        }
        composable<RunAllTests> { backStackEntry ->
            RunAllTestsScreen(
                onDone = { navController.popBackStackFrom(backStackEntry) },
                onOpenCategory = { route -> navController.openReportRetest(backStackEntry, route) },
                onDisplayFullscreenChange = onDisplayFullscreenChange,
                showTestWarnings = appPreferences.testWarningsEnabled,
            )
        }
        composable<Settings> { backStackEntry ->
            SettingsRoute(
                onOpenLanguage = { navController.navigateFrom(backStackEntry, LanguageSettings) },
                onOpenLicenses = { navController.navigateFrom(backStackEntry, Licenses) },
                onOpenOnboarding = { navController.navigateFrom(backStackEntry, Onboarding(reopened = true)) },
            )
        }
        composable<LanguageSettings> { LanguageSelectionRoute() }
        composable<Licenses> { LicensesScreen() }
        composable<Onboarding> { backStackEntry ->
            val route = backStackEntry.toRoute<Onboarding>()
            OnboardingRoute(
                onFinish = {
                    if (navController.currentBackStackEntry !== backStackEntry) return@OnboardingRoute
                    if (route.reopened) {
                        navController.popBackStackFrom(backStackEntry)
                    } else {
                        navController.navigate(Home) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable<Report> { backStackEntry ->
            ReportDetailRoute(
                onBack = { navController.popBackStackFrom(backStackEntry) },
                onRetest = { route -> navController.openReportRetest(backStackEntry, route) },
            )
        }
        composable<CategoryRetest> { backStackEntry ->
            val route = backStackEntry.toRoute<CategoryRetest>()
            val category = DiagnosticCategoryId.entries.firstOrNull { it.stableId == route.categoryId }
            if (category == null) {
                ScreenStateScreen(
                    type = ScreenStateType.UNAVAILABLE,
                    message = stringResource(R.string.report_retest_unavailable),
                )
            } else {
                RunAllTestsScreen(
                    onDone = { navController.popBackStackFrom(backStackEntry) },
                    onOpenCategory = { destination -> navController.openReportRetest(backStackEntry, destination) },
                    onDisplayFullscreenChange = onDisplayFullscreenChange,
                    targetCategory = category,
                    showTestWarnings = appPreferences.testWarningsEnabled,
                )
            }
        }
        composable<History> { backStackEntry ->
            HistoryRoute(
                onOpen = { reportId -> navController.navigateFrom(backStackEntry, Report(reportId)) },
                onCompare = { firstReportId, secondReportId ->
                    navController.navigateFrom(backStackEntry, ReportComparison(firstReportId, secondReportId))
                },
                onExport = { reportId -> navController.navigateFrom(backStackEntry, ReportExport(reportId)) },
            )
        }
        composable<ReportComparison> { backStackEntry ->
            ReportComparisonRoute(onBack = { navController.popBackStackFrom(backStackEntry) })
        }
        composable<ReportExport> { backStackEntry ->
            ReportExportRoute(onBack = { navController.popBackStackFrom(backStackEntry) })
        }
    }
}

internal fun NavHostController.popBackStackFrom(source: NavBackStackEntry) {
    if (currentBackStackEntry === source) popBackStack()
}

private fun NavHostController.navigateFrom(
    source: NavBackStackEntry,
    route: Any,
) {
    if (currentBackStackEntry === source && source.lifecycle.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

private fun NavHostController.openReportRetest(
    source: NavBackStackEntry,
    route: Any,
) {
    // A second click from the outgoing report must not push another run. Do not use
    // singleTop: a deliberate retest of the same category needs a new ViewModelStore.
    if (route is CategoryRetest && currentBackStackEntry === source &&
        source.lifecycle.currentState == Lifecycle.State.RESUMED
    ) {
        navigate(route)
    }
}

internal fun initialDestination(preferences: AppPreferences): Any =
    if (preferences.onboardingComplete) Home else Onboarding()
