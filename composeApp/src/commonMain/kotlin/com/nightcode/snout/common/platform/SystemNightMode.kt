package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode

/**
 * Tells the platform which theme the user picked, so UI the platform draws before the app (Android's splash)
 * matches it. [themeMode] is null until the stored choice has loaded; nothing is applied before then.
 */
@Composable
internal expect fun SystemNightMode(themeMode: ThemeMode?)
