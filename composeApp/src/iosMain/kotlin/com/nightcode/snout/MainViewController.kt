package com.nightcode.snout

import androidx.compose.ui.window.ComposeUIViewController

/** Starts the app's graph; call once when the app launches, before the first [MainViewController]. */
fun startSnout() = startSnoutKoin {}

/** iOS has no launch screen to hold, so nothing listens for App's onReady. */
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { App() }
