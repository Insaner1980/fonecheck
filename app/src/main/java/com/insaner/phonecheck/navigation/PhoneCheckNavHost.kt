package com.insaner.phonecheck.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.insaner.phonecheck.ui.screens.deviceinfo.DeviceInfoScreen
import com.insaner.phonecheck.ui.screens.performance.PerformanceInfoScreen
import com.insaner.phonecheck.ui.screens.simtelephony.SimTelephonyScreen

@Composable
fun PhoneCheckNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier,
    ) {
        composable<Home> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("PhoneCheck")
            }
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
    }
}
