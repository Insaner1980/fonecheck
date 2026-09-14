package com.insaner.fonecheck.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.localization.confidenceStringRes

@Composable
fun confidenceLabel(confidence: Confidence): String = stringResource(confidenceStringRes(confidence))
