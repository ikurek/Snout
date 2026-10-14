package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Calls [onEntered] each time the screen is entered, that is each time its lifecycle is resumed: when it first shows
 * (or comes back into a tab), when the screen pushed over it is popped (Navigation 3 resumes an entry only once its
 * transition has settled), and when the app returns from the background (on iOS too, where Compose resumes the
 * lifecycle when the scene enters the foreground). Routes pass their view model's `onScreenEntered`, which refreshes
 * as pull to refresh does, except on the first call (the first load is already running or done) and while a load
 * runs.
 */
@Composable
internal fun RefreshOnEnter(onEntered: () -> Unit) {
    val currentOnEntered by rememberUpdatedState(onEntered)
    LifecycleResumeEffect(Unit) {
        currentOnEntered()
        onPauseOrDispose {}
    }
}
