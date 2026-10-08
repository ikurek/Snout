package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode

/**
 * Tells the platform which theme the user picked, so UI the platform draws matches it: Android's splash, iOS's
 * status bar, keyboard and alerts. [themeMode] is null until the stored choice has loaded; nothing is applied
 * before then.
 */
@Composable
internal expect fun SystemNightMode(themeMode: ThemeMode?)
