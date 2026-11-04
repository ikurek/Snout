package com.nightcode.snout

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSURLCache
import platform.UIKit.UIViewController

/**
 * Starts the app's graph; call once when the app launches, before the first [MainViewController].
 *
 * Also empties the shared URL cache: earlier versions let URLSession store PostHog responses there
 * (`Library/Caches/<bundle id>/Cache.db`). The API client no longer uses it; the image loader still may, for
 * logos, which this clears on every launch too. Then starts reporting, which needs one local read (the person's
 * choice), taking milliseconds.
 */
fun startSnout() {
    NSURLCache.sharedURLCache.removeAllCachedResponses()
    startSnoutKoin {}
    runBlocking { applyTelemetrySettings() }
}

/**
 * The app's view controller, opening on the [Startup] it reads first. iOS can't hold its launch screen past the
 * first frame, so this blocks the main thread for that read: it only touches local storage (DataStore and the
 * Keychain-backed secure store) and takes milliseconds, while the launch screen is still up, and the first frame is
 * then the right screen in the right theme.
 *
 * Rendering is parallel: Compose encodes the frame's Metal commands on a render thread instead of the main thread,
 * leaving the main thread more of each frame (8 ms at 120 Hz) for composition, layout and input. The app hosts no
 * UIKit views inside Compose, the case parallel rendering can't synchronise with.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController {
    val startup = runBlocking { loadStartup() }
    return ComposeUIViewController(configure = { parallelRendering = true }) { App(startup) }
}
