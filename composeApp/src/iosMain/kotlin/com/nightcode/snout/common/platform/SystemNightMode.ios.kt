package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * Overrides the interface style of the app's windows, so the status bar, keyboard and system alerts follow the
 * user's choice instead of the system's. The launch screen is drawn before any app code runs and can't follow it.
 */
@Composable
internal actual fun SystemNightMode(themeMode: ThemeMode?) {
    LaunchedEffect(themeMode) {
        val style = when (themeMode) {
            null -> return@LaunchedEffect
            ThemeMode.System -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            ThemeMode.Light -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            ThemeMode.Dark -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
            .forEach { it.overrideUserInterfaceStyle = style }
    }
}
