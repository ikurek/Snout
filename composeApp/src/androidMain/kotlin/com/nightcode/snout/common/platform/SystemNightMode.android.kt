package com.nightcode.snout.common.platform

import android.app.UiModeManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode

/**
 * Android 12+ draws the splash from the app's night mode, which follows the system unless the app sets its own.
 * Changing it recreates the activity once; later launches open straight into the matching splash.
 */
@Composable
internal actual fun SystemNightMode(themeMode: ThemeMode?) {
    val context = LocalContext.current
    LaunchedEffect(themeMode) {
        if (themeMode == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@LaunchedEffect
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
