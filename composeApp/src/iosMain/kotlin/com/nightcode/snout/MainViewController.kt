package com.nightcode.snout

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSURLCache

/**
 * Starts the app's graph; call once when the app launches, before the first [MainViewController].
 *
 * Also empties the shared URL cache: earlier versions let URLSession store PostHog responses there
 * (`Library/Caches/<bundle id>/Cache.db`). The API client no longer uses it; the image loader still may, for
 * logos, which this clears on every launch too.
 */
fun startSnout() {
    NSURLCache.sharedURLCache.removeAllCachedResponses()
    startSnoutKoin {}
}

/** iOS has no launch screen to hold, so nothing listens for App's onReady. */
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { App() }
