package com.insaner.fonecheck.navigation

import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import com.insaner.fonecheck.R
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class NavigationChromeTest {
    @Test
    fun `home owns its header without the shared top bar`() {
        val chrome = navigationChromeFor(destinationFor<Home>())

        assertEquals(R.string.app_name, chrome.titleResId)
        assertFalse(chrome.showBackAction)
        assertFalse(chrome.showTopBar)
    }

    @Test
    fun `every diagnostic destination uses its shared label and diagnostic chrome`() {
        diagnosticDestinations.forEach { destination ->
            val chrome = navigationChromeFor(destinationFor(destination.route::class))

            assertEquals(destination.labelResId, chrome.titleResId)
            assertTrue(chrome.showBackAction)
            assertTrue(chrome.showTopBar)
        }
    }

    @Test
    fun `primary routes have matching titles and back actions`() {
        val routes =
            mapOf(
                destinationFor<RunAllTests>() to R.string.full_check_title,
                destinationFor<CategoryRetest>() to R.string.report_retest,
                destinationFor<Settings>() to R.string.settings_title,
                destinationFor<LanguageSettings>() to R.string.settings_language,
                destinationFor<Licenses>() to R.string.licenses_title,
                destinationFor<Onboarding>() to R.string.onboarding_title,
                destinationFor<History>() to R.string.history_title,
            )

        routes.forEach { (route, titleResId) ->
            val chrome = navigationChromeFor(route)
            assertEquals(titleResId, chrome.titleResId)
            assertTrue(chrome.showBackAction)
            assertTrue(chrome.showTopBar)
        }
    }

    @Test
    fun `argument routes match their typed destinations`() {
        assertEquals(
            R.string.report_saved_title,
            navigationChromeFor(destinationFor<Report>()).titleResId,
        )
        assertEquals(
            R.string.comparison_title,
            navigationChromeFor(destinationFor<ReportComparison>()).titleResId,
        )
        assertEquals(
            R.string.export_title,
            navigationChromeFor(destinationFor<ReportExport>()).titleResId,
        )
    }

    private inline fun <reified T : Any> destinationFor(): NavDestination = destinationFor(T::class)

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private fun <T : Any> destinationFor(route: KClass<T>): NavDestination {
        val descriptor = route.serializer().descriptor
        var routeId = descriptor.serialName.hashCode()
        repeat(descriptor.elementsCount) { index ->
            routeId = 31 * routeId + descriptor.getElementName(index).hashCode()
        }
        return NavDestination(TestNavigator()).apply { id = routeId }
    }

    @Navigator.Name("test")
    private class TestNavigator : Navigator<NavDestination>() {
        override fun createDestination(): NavDestination = NavDestination(this)
    }
}
