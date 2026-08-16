package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * A hairline divider: one physical pixel at any density, so it stays a hairline instead of thickening
 * on high-density screens the way a 1dp line would.
 */
@Composable
fun HairlineRule(modifier: Modifier = Modifier) {
    val thickness = with(LocalDensity.current) { 1f.toDp() }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(thickness)
                .background(FonecheckTheme.colors.ruleHairline),
    )
}

/** The full-weight rule that sits under a section label. Used nowhere else. */
@Composable
fun StrongRule(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FonecheckTheme.spacing.ruleThickness)
                .background(FonecheckTheme.colors.ruleStrong),
    )
}
