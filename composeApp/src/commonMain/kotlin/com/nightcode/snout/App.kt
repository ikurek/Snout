package com.nightcode.snout

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcode.snout.common.platform.PlatformThemeEffects
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.navigation.MainNavGraph
import com.nightcode.snout.navigation.toNavKey
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import org.koin.compose.koinInject

/**
 * The whole app, on a Koin graph the platform started first (see [startSnoutKoin]), opening on what [startup]
 * read before the first frame. The theme follows later changes in settings.
 */
@Composable
fun App(startup: Startup) {
    val observeThemeMode = koinInject<ObserveThemeMode>()
    val themeModes = remember(observeThemeMode) { observeThemeMode() }
    val themeMode by themeModes.collectAsStateWithLifecycle(startup.themeMode)
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    PlatformThemeEffects(themeMode = themeMode, darkTheme = darkTheme)

    SnoutTheme(darkTheme = darkTheme) {
        // Paint the theme background edge to edge: screens that pad themselves away from the system bars
        // (login, project selection) would otherwise show the host's default white on iOS.
        Surface(modifier = Modifier.fillMaxSize()) {
            MainNavGraph(
                initial = startup.initialLocation.toNavKey(),
                initialNavigationPreferences = startup.navigationPreferences,
            )
        }
    }
}
