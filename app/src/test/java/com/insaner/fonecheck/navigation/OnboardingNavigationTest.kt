package com.insaner.fonecheck.navigation

import com.insaner.fonecheck.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingNavigationTest {
    @Test
    fun `incomplete first run starts in onboarding`() {
        assertTrue(initialDestination(AppPreferences(onboardingComplete = false)) is Onboarding)
    }

    @Test
    fun `completed onboarding starts at home`() {
        assertEquals(Home, initialDestination(AppPreferences(onboardingComplete = true)))
    }
}
