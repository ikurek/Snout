package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable

/** Compose on iOS already derives the status bar style from the content drawn under it. */
@Composable
internal actual fun SystemBarsAppearance(darkTheme: Boolean) = Unit
