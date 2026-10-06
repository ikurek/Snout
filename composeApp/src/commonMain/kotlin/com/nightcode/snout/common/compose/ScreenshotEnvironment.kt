package com.nightcode.snout.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.nightcode.snout.common.format.LocalNow
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import kotlin.time.Instant

/** Deterministic rendering environment for screenshot tests: theme, surface background and a frozen clock. */
@Composable
fun ScreenshotEnvironment(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNow provides Instant.parse("2026-10-06T12:00:00Z")) {
        SnoutTheme(darkTheme = darkTheme) {
            Surface(content = content)
        }
    }
}
