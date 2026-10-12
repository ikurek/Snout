package com.nightcode.snout

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.common.platform.SystemBarsAppearance
import com.nightcode.snout.common.platform.SystemNightMode
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.features.links.IncomingLinks
import com.nightcode.snout.navigation.MainNavGraph
import com.nightcode.snout.navigation.toNavKey
import com.nightcode.snout.shared.domain.model.appstate.NavigationPreferences
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.usecase.appstate.DetermineInitialLocation
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveNavigationPreferences
import com.nightcode.snout.shared.domain.usecase.appstate.ObserveThemeMode
import com.nightcode.snout.shared.domain.usecase.links.SubmitIncomingLink
import kotlinx.coroutines.flow.first
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import org.koin.plugin.module.dsl.module

/** Everything the first screen needs from storage, read before anything is drawn. */
private class Startup(val initial: NavKey, val navigationPreferences: NavigationPreferences)

/**
 * [onReady] fires once the startup reads and the theme are in and the first screen is composed; Android keeps
 * its system splash up until then, so the app never shows an empty frame of its own. [incomingLinks] brings in
 * links the platform was asked to open.
 */
@Composable
fun App(onReady: () -> Unit = {}, incomingLinks: IncomingLinks? = null) = KoinApplication(
    configuration = koinConfiguration {
        module<AppModule>()
    },
) {
    ReceiveIncomingLinks(incomingLinks)

    val determineInitialLocation = koinInject<DetermineInitialLocation>()
    val observeNavigationPreferences = koinInject<ObserveNavigationPreferences>()
    val startup by produceState<Startup?>(initialValue = null) {
        value = Startup(
            initial = determineInitialLocation().toNavKey(),
            // Home's tabs come from preferences; reading them here lets it draw on its first frame.
            navigationPreferences = observeNavigationPreferences().first(),
        )
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
            val loaded = startup
            if (loaded != null && storedThemeMode != null) {
                MainNavGraph(initial = loaded.initial, initialNavigationPreferences = loaded.navigationPreferences)
                SideEffect(onReady)
            }
        }
    }
}

/** Queues each link the platform hands over; the navigation graph opens it once it can. */
@Composable
private fun ReceiveIncomingLinks(incomingLinks: IncomingLinks?) {
    val submitIncomingLink = koinInject<SubmitIncomingLink>()
    LaunchedEffect(incomingLinks) {
        incomingLinks?.receive()?.collect { submitIncomingLink(it) }
    }
}
