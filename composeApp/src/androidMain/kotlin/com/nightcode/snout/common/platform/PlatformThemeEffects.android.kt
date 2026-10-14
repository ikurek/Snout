package com.nightcode.snout.common.platform

import android.app.Activity
import android.app.UiModeManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode

@Composable
internal actual fun PlatformThemeEffects(themeMode: ThemeMode, darkTheme: Boolean) {
    ApplicationNightMode(themeMode)
    SystemBarsAppearance(darkTheme)
}

/**
 * Android 12+ draws the splash from the app's night mode, which follows the system unless the app sets its own.
 * Changing it recreates the activity once; later launches open straight into the matching splash.
 */
@Composable
private fun ApplicationNightMode(themeMode: ThemeMode) {
    val context = LocalContext.current
    LaunchedEffect(themeMode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@LaunchedEffect
        val uiModeManager = context.getSystemService(UiModeManager::class.java) ?: return@LaunchedEffect
        uiModeManager.setApplicationNightMode(
            when (themeMode) {
                ThemeMode.System -> UiModeManager.MODE_NIGHT_AUTO
                ThemeMode.Light -> UiModeManager.MODE_NIGHT_NO
                ThemeMode.Dark -> UiModeManager.MODE_NIGHT_YES
            },
        )
    }
}

/** Keeps the status and navigation bar icons readable against the app's theme. */
@Composable
private fun SystemBarsAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
