package com.nightcode.snout.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Keeps the platform's splash up until the first screen can draw what it is meant to show, so the app never shows
 * an empty or wrong frame of its own. The app calls [onStartScreenComposed] once its start screen is in; a screen
 * still reading something it can't draw without holds the gate shut meanwhile with [HoldSplash].
 */
internal class SplashGate(private val onReady: () -> Unit) {
    private var holds = 0
    private var startScreenComposed = false
    private var opened = false

    fun onStartScreenComposed() {
        startScreenComposed = true
        openIfReady()
    }

    fun hold() {
        holds++
    }

    fun release() {
        holds--
        openIfReady()
    }

    private fun openIfReady() {
        if (startScreenComposed && holds == 0 && !opened) {
            opened = true
            onReady()
        }
    }
}

internal val LocalSplashGate = staticCompositionLocalOf<SplashGate?> { null }

/**
 * Holds the splash for as long as this is composed. Lifecycle observers run before side effects, so a hold taken in
 * the start screen's first composition is in place before the app opens the gate.
 */
@Composable
internal fun HoldSplash() {
    val gate = LocalSplashGate.current ?: return
    DisposableEffect(gate) {
        gate.hold()
        onDispose { gate.release() }
    }
}
