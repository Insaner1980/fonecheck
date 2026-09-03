package com.insaner.fonecheck.ui.screens.connectivity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectivityPresentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun satellitesSeparateIdentitySignalAndFixUsage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = context.resources.configuration.locales[0]
        val satellites =
            listOf(
                GpsSatelliteInfo(7, "GPS", 30.5f, true, 45f, 90f),
                GpsSatelliteInfo(12, "Galileo", 20f, false, 30f, 180f),
            )

        composeRule.setContent {
            FonecheckTheme {
                Box(Modifier.width(240.dp)) {
                    SatelliteSection(satellites)
                }
            }
        }

        val usedLabel = context.getString(R.string.conn_gps_satellite_label, "7", "GPS")
        val unusedLabel = context.getString(R.string.conn_gps_satellite_label, "12", "Galileo")
        val usedValue =
            context.getString(
                R.string.conn_gps_satellite_value,
                formatUiNumber(30.5, locale, 1, 1),
                context.getString(R.string.conn_gps_satellite_used_in_fix),
            )
        val unusedValue =
            context.getString(
                R.string.conn_gps_satellite_value,
                formatUiNumber(20.0, locale, 1, 1),
                context.getString(R.string.conn_gps_satellite_not_used_in_fix),
            )

        composeRule.onNodeWithContentDescription(usedLabel).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(usedValue).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(unusedLabel).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(unusedValue).assertIsDisplayed()
    }

    @Test
    fun bluetoothDetailsTrustMeasuredAccessInsteadOfStaleUiPermissionState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FonecheckTheme {
                BluetoothDetails(
                    bluetooth =
                        BluetoothState(
                            isAvailable = true,
                            access = BluetoothAccessCode.PERMISSION_DENIED,
                            isEnabled = false,
                            name = "Private adapter",
                            bleSupported = true,
                            bondedDeviceCount = 7,
                        ),
                    permissionState = PermissionState.GRANTED,
                    onRequestPermission = {},
                    onOpenSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("Private adapter").assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.status_disabled)).assertDoesNotExist()
        composeRule.onNodeWithText("7").assertDoesNotExist()
    }
}
