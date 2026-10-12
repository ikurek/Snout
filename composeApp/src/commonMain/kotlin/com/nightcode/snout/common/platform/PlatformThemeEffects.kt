package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode

/**
 * Tells the platform which theme is showing, so what it draws matches the app's, which can differ from the
 * system's when the person picks Light or Dark in settings: Android's splash and system bar icons, iOS's status
 * bar, keyboard and alerts. [themeMode] is the stored choice, null until it has loaded (nothing is applied before
 * then); [darkTheme] is what that choice resolves to.
 */
@Composable
internal expect fun PlatformThemeEffects(themeMode: ThemeMode?, darkTheme: Boolean)
