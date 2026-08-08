package com.insaner.fonecheck.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.data.preferences.AppPreferences
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
import com.insaner.fonecheck.ui.screens.home.HomeScreen
import com.insaner.fonecheck.ui.screens.history.HistoryRoute
import com.insaner.fonecheck.ui.screens.onboarding.OnboardingRoute
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoScreen
import com.insaner.fonecheck.ui.screens.report.ReportDetailRoute
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsScreen
import com.insaner.fonecheck.ui.screens.sensor.SensorTestScreen
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
    onDisplayFullscreenChanged: (Boolean) -> Unit = {},
    appPreferences: AppPreferences = AppPreferences(),
) {
    NavHost(
        navController = navController,
        startDestination = initialDestination(appPreferences),
        modifier = modifier,
    ) {
        composable<Home> {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                onRunAllTests = { navController.navigate(RunAllTests) },
            )
        }
        composable<DeviceInfo> {
            DeviceInfoScreen()
        }
        composable<PerformanceInfo> {
            PerformanceInfoScreen()
        }
        composable<SimTelephony> {
            SimTelephonyScreen()
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
        composable<ConnectivityTest> {
            ConnectivityTestScreen()
        }
        composable<BatteryTest> {
            BatteryTestScreen()
        }
        composable<ThermalTest> {
            ThermalTestScreen()
        }
        composable<StorageTest> {
            StorageTestScreen()
        }
        composable<DisplayTest> {
            DisplayTestScreen(onFullscreenChanged = onDisplayFullscreenChanged)
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
        composable<RunAllTests> {
            RunAllTestsScreen(
                onDone = { navController.popBackStack() },
                onOpenCategory = { route -> navController.navigate(route) },
                onDisplayFullscreenChanged = onDisplayFullscreenChanged,
                showTestWarnings = appPreferences.testWarningsEnabled,
            )
        }
        composable<Settings> {
            SettingsRoute(
                onOpenLicenses = { navController.navigate(Licenses) },
                onOpenOnboarding = { navController.navigate(Onboarding(reopened = true)) },
            )
        }
        composable<Licenses> { LicensesScreen() }
        composable<Onboarding> { backStackEntry ->
            val route = backStackEntry.toRoute<Onboarding>()
            OnboardingRoute(
                onFinished = {
                    if (route.reopened) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Home) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable<Report> {
            ReportDetailRoute(
                onBack = { navController.popBackStack() },
                onRetest = { route -> navController.navigate(route) },
            )
        }
        composable<CategoryRetest> { backStackEntry ->
            val route = backStackEntry.toRoute<CategoryRetest>()
            val category = DiagnosticCategoryId.entries.firstOrNull { it.stableId == route.categoryId }
            if (category == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.report_retest_unavailable))
                }
            } else {
                RunAllTestsScreen(
                    onDone = { navController.popBackStack() },
                    onOpenCategory = { destination -> navController.navigate(destination) },
                    onDisplayFullscreenChanged = onDisplayFullscreenChanged,
                    targetCategory = category,
                    showTestWarnings = appPreferences.testWarningsEnabled,
                )
            }
        }
        composable<History> {
            HistoryRoute(
                onOpen = { reportId -> navController.navigate(Report(reportId)) },
                onCompare = { firstReportId, secondReportId ->
                    navController.navigate(ReportComparison(firstReportId, secondReportId))
                },
                onExport = { reportId -> navController.navigate(ReportExport(reportId)) },
            )
        }
        composable<ReportComparison> {
            ReportComparisonRoute(onBack = { navController.popBackStack() })
        }
        composable<ReportExport> {
            ReportExportRoute(onBack = { navController.popBackStack() })
        }
    }
}

internal fun initialDestination(preferences: AppPreferences): Any =
    if (preferences.onboardingComplete) Home else Onboarding()
