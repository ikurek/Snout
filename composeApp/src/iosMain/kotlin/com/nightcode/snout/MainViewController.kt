package com.nightcode.snout

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSURLCache
import platform.UIKit.UIViewController

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

/**
 * The app's view controller, opening on the [Startup] it reads first. iOS can't hold its launch screen past the
 * first frame, so this blocks the main thread for that read: it only touches local storage (DataStore and the
 * Keychain-backed secure store) and takes milliseconds, while the launch screen is still up, and the first frame is
 * then the right screen in the right theme.
 */
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController {
    val startup = runBlocking { loadStartup() }
    return ComposeUIViewController { App(startup) }
}
