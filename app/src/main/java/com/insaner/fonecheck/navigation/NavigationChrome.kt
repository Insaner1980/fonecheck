package com.insaner.fonecheck.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.insaner.fonecheck.R
import kotlin.reflect.KClass

internal data class NavigationChrome(
    @StringRes val titleResId: Int,
    val showBackAction: Boolean,
    val showTopBar: Boolean = true,
)

internal fun navigationChromeFor(destination: NavDestination?): NavigationChrome {
    val diagnosticDestination = diagnosticDestinations.firstOrNull { destination.matches(it.route::class) }
    return when {
        destination.matches(Home::class) ->
            NavigationChrome(
                R.string.app_name,
                showBackAction = false,
                showTopBar = false,
            )
        diagnosticDestination != null -> NavigationChrome(diagnosticDestination.labelResId, showBackAction = true)
        destination.matches(RunAllTests::class) -> NavigationChrome(R.string.full_check_title, showBackAction = true)
        destination.matches(FullAccess::class) -> NavigationChrome(R.string.full_access_title, showBackAction = true)
        destination.matches(Settings::class) -> NavigationChrome(R.string.settings_title, showBackAction = true)
        destination.matches(LanguageSettings::class) ->
            NavigationChrome(R.string.settings_language, showBackAction = true)
        destination.matches(Licenses::class) -> NavigationChrome(R.string.licenses_title, showBackAction = true)
        destination.matches(Report::class) ->
            NavigationChrome(R.string.report_saved_title, showBackAction = true)
        destination.matches(CategoryRetest::class) ->
            NavigationChrome(R.string.report_retest, showBackAction = true)
        destination.matches(History::class) -> NavigationChrome(R.string.history_title, showBackAction = true)
        destination.matches(ReportComparison::class) ->
            NavigationChrome(R.string.comparison_title, showBackAction = true)
        destination.matches(ReportExport::class) ->
            NavigationChrome(R.string.export_title, showBackAction = true)
        else -> NavigationChrome(R.string.app_name, showBackAction = destination != null)
    }
}

private fun <T : Any> NavDestination?.matches(route: KClass<T>): Boolean = this?.hasRoute(route) == true
