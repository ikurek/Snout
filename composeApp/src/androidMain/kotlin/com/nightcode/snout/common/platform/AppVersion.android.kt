package com.nightcode.snout.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun appVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        // Preview and screenshot renderers have no real package manager.
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
            .getOrDefault("1.0")
    }
}
