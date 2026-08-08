package com.insaner.fonecheck.ui

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.navigation.FonecheckNavHost
import com.insaner.fonecheck.navigation.Home
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonEventSource
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonKeyMapper
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var volumeButtonEventSource: VolumeButtonEventSource

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        VolumeButtonKeyMapper.directionFor(
            keyCode = keyCode,
            action = KeyEvent.ACTION_DOWN,
            repeatCount = event.repeatCount,
        )?.let(volumeButtonEventSource::record)
        return super.onKeyDown(keyCode, event)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartedAt = SystemClock.uptimeMillis()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val hasAnimatedSystemSplash = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        splashScreen.setKeepOnScreenCondition {
            hasAnimatedSystemSplash &&
                SystemClock.uptimeMillis() - splashStartedAt < SPLASH_MIN_DURATION_MS
        }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
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
        setContent {
            val preferences by
                appPreferencesRepository.preferences.collectAsStateWithLifecycle(
                    initialValue = AppPreferences(),
                )
            FonecheckTheme(
                darkTheme = preferences.themeMode.resolveDarkTheme(isSystemInDarkTheme()),
            ) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                var isDisplayFullscreen by remember { mutableStateOf(false) }
                val isHome =
                    currentRoute == null ||
                        currentRoute == Home::class.qualifiedName

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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!isDisplayFullscreen) {
                            TopAppBar(
                                title = { Text(stringResource(R.string.app_name)) },
                                navigationIcon = {
                                    if (!isHome) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.navigation_back),
                                            )
                                        }
                                    }
                                },
                                colors =
                                    TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                            )
                        }
                    },
                ) { innerPadding ->
                    FonecheckNavHost(
                        navController = navController,
                        appPreferences = preferences,
                        modifier =
                            if (isDisplayFullscreen) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.padding(innerPadding)
                            },
                        onDisplayFullscreenChanged = { isDisplayFullscreen = it },
                    )
                }
            }
        }
    }

    private companion object {
        const val SPLASH_MIN_DURATION_MS = 1_500L
        const val SPLASH_EXIT_DURATION_MS = 180L
        const val SPLASH_EXIT_SCALE = 0.92f
    }
}
