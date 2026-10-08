package com.nightcode.snout

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.compose.LocalStartupReady
import com.nightcode.snout.common.compose.StartupReady
import com.nightcode.snout.common.platform.SystemBarsAppearance
import com.nightcode.snout.common.platform.SystemNightMode
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.home.HomeNavKey
import com.nightcode.snout.features.intialproject.InitialProjectNavKey
import com.nightcode.snout.features.login.LoginNavKey
import com.nightcode.snout.navigation.MainNavGraph
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.appstate.InitialLocation
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import org.koin.plugin.module.dsl.module

/**
 * [onReady] fires once the theme is known and the start screen reports content through [StartupReady];
 * Android keeps its system splash up until then, so the app never shows an empty frame of its own.
 */
@Composable
fun App(onReady: () -> Unit = {}) = KoinApplication(
    configuration = koinConfiguration {
        module<AppModule>()
    },
) {
    val determineInitialLocation = koinInject<DetermineInitialLocation>()
    val initial by produceState<NavKey?>(initialValue = null) {
        value = when (determineInitialLocation()) {
            InitialLocation.Login -> LoginNavKey
            InitialLocation.ProjectSelection -> InitialProjectNavKey
            InitialLocation.Home -> HomeNavKey
        }
    }

    val observeThemeMode = koinInject<ObserveThemeMode>()
    // Null until the stored choice loads, so the platform is never told the placeholder.
    val storedThemeMode by observeThemeMode().collectAsStateWithLifecycle<ThemeMode?>(null)
    SystemNightMode(storedThemeMode)
    val darkTheme = when (storedThemeMode ?: ThemeMode.System) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    SystemBarsAppearance(darkTheme = darkTheme)
    SnoutTheme(darkTheme = darkTheme) {
        // Paint the theme background edge to edge: screens that pad themselves away from the system bars
        // (login, project selection) would otherwise show the host's default white on iOS.
        Surface(modifier = Modifier.fillMaxSize()) {
            // Wait for the theme too, so the first frame is never drawn in the wrong one.
            val key = initial
            if (key != null && storedThemeMode != null) {
                CompositionLocalProvider(LocalStartupReady provides onReady) {
                    MainNavGraph(initial = key)
                }
            }
        }
    }
}
