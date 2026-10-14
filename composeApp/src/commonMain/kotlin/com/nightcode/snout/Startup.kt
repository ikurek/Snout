package com.nightcode.snout

import com.nightcode.snout.shared.domain.model.appstate.InitialLocation
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import kotlinx.coroutines.flow.first
import org.koin.mp.KoinPlatform

/**
 * What the first frame needs: the first screen, the theme and, for Home, its bar and landing tab. The platform loads
 * it while its launch screen is up and hands it to [App], so the app opens straight into the right screen and theme.
 */
class Startup internal constructor(
    internal val initialLocation: InitialLocation,
    internal val themeMode: ThemeMode,
    internal val navigationPreferences: NavigationPreferences,
)

/** Reads [Startup] from local storage only (no network), after the platform started the graph. */
suspend fun loadStartup(): Startup {
    val koin = KoinPlatform.getKoin()
    return Startup(
        initialLocation = koin.get<DetermineInitialLocation>().invoke(),
        themeMode = koin.get<ObserveThemeMode>().invoke().first(),
        navigationPreferences = koin.get<ObserveNavigationPreferences>().invoke().first(),
    )
}
