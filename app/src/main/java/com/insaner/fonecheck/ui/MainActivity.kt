package com.insaner.fonecheck.ui

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.navigation.FonecheckNavHost
import com.insaner.fonecheck.navigation.navigationChromeFor
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonEventSource
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonKeyMapper
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val HousingHorizontalInset = 6.dp

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var volumeButtonEventSource: VolumeButtonEventSource

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        VolumeButtonKeyMapper
            .directionFor(
                keyCode = keyCode,
                action = KeyEvent.ACTION_DOWN,
                repeatCount = event.repeatCount,
            )?.let(volumeButtonEventSource::record)
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartedAt = SystemClock.uptimeMillis()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val shouldAnimateSplash = shouldAnimateSplash()
        splashScreen.setKeepOnScreenCondition {
            shouldAnimateSplash &&
                SystemClock.uptimeMillis() - splashStartedAt < SPLASH_MIN_DURATION_MS
        }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            if (!shouldAnimateSplash) {
                splashScreenView.remove()
                return@setOnExitAnimationListener
            }
            splashScreenView.iconView
                .animate()
                .scaleX(SPLASH_EXIT_SCALE)
                .scaleY(SPLASH_EXIT_SCALE)
                .setDuration(SPLASH_EXIT_DURATION_MS)
                .start()

            splashScreenView.view
                .animate()
                .alpha(0f)
                .setDuration(SPLASH_EXIT_DURATION_MS)
                .withEndAction { splashScreenView.remove() }
                .start()
        }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            FonecheckContent()
        }
    }

    private fun shouldAnimateSplash(): Boolean =
        shouldAnimateSplash(
            systemSupportsAnimatedSplash = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            animatorsEnabled = ValueAnimator.areAnimatorsEnabled(),
        )

    @Composable
    private fun FonecheckContent() {
        val preferenceFlow =
            remember(appPreferencesRepository) {
                appPreferencesRepository.preferences.map<AppPreferences, AppPreferences?> { it }
            }
        val preferences by preferenceFlow.collectAsStateWithLifecycle(initialValue = null)
        val darkTheme =
            (preferences?.themeMode ?: AppThemeMode.SYSTEM).resolveDarkTheme(isSystemInDarkTheme())
        FonecheckTheme(darkTheme = darkTheme) {
            val loadedPreferences = preferences
            if (loadedPreferences == null) {
                ConfigureSystemBars(isDisplayFullscreen = false)
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(FonecheckTheme.colors.housing),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .padding(horizontal = HousingHorizontalInset)
                                .background(FonecheckTheme.colors.background)
                                .clipToBounds(),
                    ) {}
                }
            } else {
                LoadedFonecheckContent(loadedPreferences)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LoadedFonecheckContent(preferences: AppPreferences) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val currentRoute = currentDestination?.route
        var isDisplayFullscreen by remember { mutableStateOf(false) }
        var topBarAction by remember(currentRoute) { mutableStateOf<TopBarAction?>(null) }
        val navigationChrome = navigationChromeFor(currentDestination)

        LaunchedEffect(currentRoute) { isDisplayFullscreen = false }
        ConfigureSystemBars(isDisplayFullscreen)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = FonecheckTheme.colors.housing,
            topBar = {
                if (!isDisplayFullscreen && navigationChrome.showTopBar) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = HousingHorizontalInset)
                                .clipToBounds(),
                    ) {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = {
                                Text(
                                    text = stringResource(navigationChrome.titleResId),
                                    modifier = Modifier.semantics { heading() },
                                )
                            },
                            navigationIcon = {
                                if (navigationChrome.showBackAction && navController.previousBackStackEntry != null) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.navigation_back),
                                        )
                                    }
                                }
                            },
                            actions = {
                                topBarAction?.let { action ->
                                    IconButton(
                                        enabled = action.enabled,
                                        onClick = action.onClick,
                                    ) {
                                        Icon(
                                            imageVector = action.icon,
                                            contentDescription = stringResource(action.contentDescriptionResId),
                                        )
                                    }
                                }
                            },
                            colors =
                                TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            windowInsets = WindowInsets(0, 0, 0, 0),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier =
                    if (isDisplayFullscreen) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = HousingHorizontalInset)
                            .background(FonecheckTheme.colors.background)
                            .clipToBounds()
                    },
            ) {
                FonecheckNavHost(
                    navController = navController,
                    appPreferences = preferences,
                    modifier = Modifier.fillMaxSize(),
                    onDisplayFullscreenChange = { isDisplayFullscreen = it },
                    onTopBarActionChange = { topBarAction = it },
                )
                if (!isDisplayFullscreen && navigationChrome.showTopBar) {
                    val surface = MaterialTheme.colorScheme.surface
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(FonecheckTheme.spacing.md)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(surface, surface.copy(alpha = 0f)),
                                    ),
                                ),
                    )
                }
            }
        }
    }

    @Composable
    private fun ConfigureSystemBars(isDisplayFullscreen: Boolean) {
        SideEffect {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
        DisposableEffect(isDisplayFullscreen) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (isDisplayFullscreen) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
            onDispose {
                if (isDisplayFullscreen) controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    private companion object {
        const val SPLASH_MIN_DURATION_MS = 1_000L
        const val SPLASH_EXIT_DURATION_MS = 180L
        const val SPLASH_EXIT_SCALE = 0.92f
    }
}

internal fun shouldAnimateSplash(
    systemSupportsAnimatedSplash: Boolean,
    animatorsEnabled: Boolean,
): Boolean = systemSupportsAnimatedSplash && animatorsEnabled
