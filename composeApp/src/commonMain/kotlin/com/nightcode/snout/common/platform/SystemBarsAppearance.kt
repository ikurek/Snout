package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable

/**
 * Keeps the status and navigation bar icons readable against the app's theme, which can differ from the system's
 * when the person picks Light or Dark in settings.
 */
@Composable
internal expect fun SystemBarsAppearance(darkTheme: Boolean)
