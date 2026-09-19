package com.insaner.fonecheck.ui.screens.access

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullAccessScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedDiagnosticIsExplainedBeforeTheFullOffer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var returned = false
        composeRule.setContent {
            FonecheckTheme {
                FullAccessScreen(
                    featureId = DiagnosticCategoryId.CAMERA.stableId,
                    onBack = { returned = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.home_cat_camera)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.full_access_camera_description))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.full_access_offer)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.full_access_return)).performScrollTo().performClick()
        assertTrue(returned)
    }
}
