package com.insaner.fonecheck.ui.screens.display

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun DisplayTestScreen(
    modifier: Modifier = Modifier,
    viewModel: DisplayTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Full-screen dead pixel test overlay
    if (state.deadPixel.isActive) {
        DeadPixelOverlay(
            colorIndex = state.deadPixel.colorIndex,
            onTap = { viewModel.nextDeadPixelColor() },
            onLongPress = { viewModel.stopDeadPixelTest() },
        )
        BackHandler { viewModel.stopDeadPixelTest() }
        return
    }

    // Full-screen burn-in test overlay
    if (state.burnIn.isActive) {
        BurnInOverlay(onLongPress = { viewModel.stopBurnInTest() })
        BackHandler { viewModel.stopBurnInTest() }
        return
    }

    TestScreenContent(modifier = modifier) {
        // Display Info
        item {
            TestSectionCard(
                icon = "DSP",
                title = stringResource(R.string.display_info_title),
                statusText = "${state.info.widthPx}\u00D7${state.info.heightPx}",
                statusColor = Blue400,
                isExpanded = state.expandedSection == DisplaySection.INFO,
                onClick = { viewModel.toggleSection(DisplaySection.INFO) },
            ) {
                DisplayInfoDetails(state.info)
            }
        }

        // Dead Pixel Test
        item {
            TestSectionCard(
                icon = "PXL",
                title = stringResource(R.string.display_dead_pixel_title),
                statusText = if (state.deadPixel.colorIndex > 0) "Tested" else "Ready",
                statusColor = Neutral500,
                isExpanded = state.expandedSection == DisplaySection.DEAD_PIXEL,
                onClick = { viewModel.toggleSection(DisplaySection.DEAD_PIXEL) },
            ) {
                DeadPixelSection(onStart = { viewModel.startDeadPixelTest() })
            }
        }

        // Touch Screen Test
        item {
            TestSectionCard(
                icon = "TCH",
                title = stringResource(R.string.display_touch_title),
                statusText =
                    "${state.touch.touchedCells.size}/" +
                        "${DisplayTestViewModel.TOUCH_GRID_COLS * DisplayTestViewModel.TOUCH_GRID_ROWS}",
                statusColor =
                    when {
                        state.touch.touchedCells.size ==
                            DisplayTestViewModel.TOUCH_GRID_COLS * DisplayTestViewModel.TOUCH_GRID_ROWS -> Green400
                        state.touch.touchedCells.isNotEmpty() -> Yellow400
                        else -> Neutral500
                    },
                isExpanded = state.expandedSection == DisplaySection.TOUCH,
                onClick = { viewModel.toggleSection(DisplaySection.TOUCH) },
            ) {
                TouchTestSection(state.touch, viewModel)
            }
        }

        // Burn-in Check
        item {
            TestSectionCard(
                icon = "BRN",
                title = stringResource(R.string.display_burnin_title),
                statusText = "Ready",
                statusColor = Neutral500,
                isExpanded = state.expandedSection == DisplaySection.BURN_IN,
                onClick = { viewModel.toggleSection(DisplaySection.BURN_IN) },
            ) {
                BurnInSection(onStart = { viewModel.startBurnInTest() })
            }
        }
    }
}

// ── Display Info Details ─────────────────────────────────────────────────────────

@Composable
private fun DisplayInfoDetails(info: DisplayInfoState) {
    SectionBox {
        DetailInfoRow(stringResource(R.string.display_resolution), "${info.widthPx} \u00D7 ${info.heightPx} px")
        DetailInfoRow(stringResource(R.string.display_density), "${info.densityDpi} dpi")
        DetailInfoRow(stringResource(R.string.display_refresh_rate), "%.0f Hz".format(info.refreshRate))
        DetailInfoRow(
            stringResource(R.string.display_hdr),
            if (info.hdrSupported) {
                stringResource(R.string.conn_supported)
            } else {
                stringResource(R.string.conn_not_supported)
            },
            valueColor = if (info.hdrSupported) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.display_wide_color),
            if (info.wideColorGamut) {
                stringResource(R.string.conn_supported)
            } else {
                stringResource(R.string.conn_not_supported)
            },
            valueColor = if (info.wideColorGamut) Green400 else Neutral500,
        )
        DetailInfoRow(stringResource(R.string.display_brightness), "${info.currentBrightness} / 255")
        DetailInfoRow(
            stringResource(R.string.display_auto_brightness),
            if (info.autoBrightness) {
                stringResource(R.string.status_enabled)
            } else {
                stringResource(R.string.status_disabled)
            },
        )
    }
}

// ── Dead Pixel Section ───────────────────────────────────────────────────────────

@Composable
private fun DeadPixelSection(onStart: () -> Unit) {
    TestLaunchSection(
        description = stringResource(R.string.display_dead_pixel_desc),
        onStart = onStart,
    )
}

// ── Touch Test Section ───────────────────────────────────────────────────────────

@Composable
private fun TouchTestSection(
    touch: TouchTestState,
    viewModel: DisplayTestViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionBox {
            Text(
                text = stringResource(R.string.display_touch_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            DetailInfoRow(
                stringResource(R.string.display_touch_points),
                "${touch.touchedCells.size} / " +
                    "${DisplayTestViewModel.TOUCH_GRID_COLS * DisplayTestViewModel.TOUCH_GRID_ROWS}",
            )
            if (touch.multiTouchCount > 1) {
                DetailInfoRow("Multi-touch", "${touch.multiTouchCount} points")
            }
        }

        // Touch grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(DisplayTestViewModel.TOUCH_GRID_COLS),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(DisplayTestViewModel.TOUCH_GRID_COLS * DisplayTestViewModel.TOUCH_GRID_ROWS) { index ->
                Box(
                    modifier =
                        Modifier
                            .aspectRatio(1f)
                            .background(
                                if (touch.touchedCells.contains(index)) {
                                    Green400.copy(alpha = 0.4f)
                                } else {
                                    Neutral700
                                },
                                RoundedCornerShape(2.dp),
                            ).clickable { viewModel.onCellTouched(index) },
                )
            }
        }

        OutlinedButton(
            onClick = { viewModel.resetTouchTest() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(stringResource(R.string.display_touch_reset))
        }
    }
}

// ── Burn-in Section ──────────────────────────────────────────────────────────────

@Composable
private fun BurnInSection(onStart: () -> Unit) {
    TestLaunchSection(
        description = stringResource(R.string.display_burnin_desc),
        onStart = onStart,
    )
}

@Composable
private fun TestLaunchSection(
    description: String,
    onStart: () -> Unit,
) {
    SectionBox {
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Blue400),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(stringResource(R.string.display_start_test))
        }
    }
}

// ── Full-screen overlays ─────────────────────────────────────────────────────────

private val deadPixelColors =
    listOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.White,
        Color.Black,
    )

@Composable
private fun DeadPixelOverlay(
    colorIndex: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(deadPixelColors[colorIndex])
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() },
                    )
                },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = stringResource(R.string.display_tap_to_cycle),
            style = MaterialTheme.typography.labelSmall,
            color = if (colorIndex == 4) Color.DarkGray else Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun BurnInOverlay(onLongPress: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFF808080))
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = stringResource(R.string.display_long_press_exit),
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}
