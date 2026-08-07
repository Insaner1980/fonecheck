package com.insaner.fonecheck.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.insaner.fonecheck.ui.screens.audio.AudioTestScreen
import com.insaner.fonecheck.ui.screens.battery.BatteryTestScreen
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestScreen
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestScreen
import com.insaner.fonecheck.ui.screens.camera.CameraTestScreen
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestScreen
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoScreen
import com.insaner.fonecheck.ui.screens.display.DisplayTestScreen
import com.insaner.fonecheck.ui.screens.home.HomeScreen
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoScreen
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsScreen
import com.insaner.fonecheck.ui.screens.sensor.SensorTestScreen
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyScreen
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestScreen

@Composable
fun FonecheckNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onDisplayFullscreenChanged: (Boolean) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = Home,
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
            )
        }
        composable<Settings> {
            PlaceholderScreen("Settings")
        }
        composable<Report> {
            PlaceholderScreen("Report")
        }
        composable<History> {
            PlaceholderScreen("History")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(title)
    }
}
