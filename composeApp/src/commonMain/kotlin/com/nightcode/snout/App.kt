package com.nightcode.snout

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.platform.PlatformThemeEffects
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
 * initial location and the stored theme are in and the start screen is composed; Android keeps its system splash
 * up until then, so the app never shows a frame in the wrong theme.
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
    val darkTheme = when (storedThemeMode ?: ThemeMode.System) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    PlatformThemeEffects(themeMode = storedThemeMode, darkTheme = darkTheme)

    SnoutTheme(darkTheme = darkTheme) {
        // Paint the theme background edge to edge: screens that pad themselves away from the system bars
        // (login, project selection) would otherwise show the host's default white on iOS.
        Surface(modifier = Modifier.fillMaxSize()) {
            // Wait for the theme too, so the first frame is never drawn in the wrong one.
            val start = initial
            if (start != null && storedThemeMode != null) {
                MainNavGraph(initial = start)
                LaunchedEffect(Unit) { onReady() }
            }
        }
    }
}
