package com.nightcode.snout

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.compose.LocalSplashGate
import com.nightcode.snout.common.compose.SplashGate
import com.nightcode.snout.common.platform.SystemBarsAppearance
import com.nightcode.snout.common.platform.SystemNightMode
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.navigation.MainNavGraph
import com.nightcode.snout.navigation.SnoutNavKey
import com.nightcode.snout.navigation.toNavKey
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import org.koin.compose.koinInject

/**
 * The whole app, on a Koin graph the platform started first (see [startSnoutKoin]). [onReady] fires once the
 * first screen can draw what it's meant to show; Android keeps its system splash up until then, so the app never
 * shows an empty frame of its own.
 */
@Composable
fun App(onReady: () -> Unit = {}) {
    val determineInitialLocation = koinInject<DetermineInitialLocation>()
    val initial by produceState<SnoutNavKey?>(initialValue = null) {
        value = determineInitialLocation().toNavKey()
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

    val currentOnReady by rememberUpdatedState(onReady)
    val splashGate = remember { SplashGate { currentOnReady() } }
    SystemBarsAppearance(darkTheme = darkTheme)
    SnoutTheme(darkTheme = darkTheme) {
        // Paint the theme background edge to edge: screens that pad themselves away from the system bars
        // (login, project selection) would otherwise show the host's default white on iOS.
        Surface(modifier = Modifier.fillMaxSize()) {
            // Wait for the theme too, so the first frame is never drawn in the wrong one.
            val start = initial
            if (start != null && storedThemeMode != null) {
                CompositionLocalProvider(LocalSplashGate provides splashGate) {
                    MainNavGraph(initial = start)
                }
                SideEffect(splashGate::onStartScreenComposed)
            }
        }
    }
}
