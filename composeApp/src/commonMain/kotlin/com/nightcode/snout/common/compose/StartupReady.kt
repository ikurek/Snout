package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf

/** Released by the first screen once it has content; Android keeps its system splash up until then. */
val LocalStartupReady = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Call from a screen the app can open on, at the point it has something to draw. Later calls do nothing, so
 * screens don't need to know whether they were the first.
 */
@Composable
internal fun StartupReady() {
    val ready = LocalStartupReady.current
    SideEffect(ready)
}
