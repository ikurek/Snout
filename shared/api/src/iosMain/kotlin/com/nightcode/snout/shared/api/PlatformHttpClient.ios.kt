package com.nightcode.snout.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.logging.Logger
import platform.Foundation.NSLog
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData

/**
 * By default the Darwin engine's session uses the shared `NSURLCache`, which writes responses to
 * `Library/Caches/<bundle id>/Cache.db`; this session has no cache at all. What earlier versions left there is
 * purged at startup (`startSnout`).
 */
internal actual fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Darwin) {
    engine {
        configureSession {
            URLCache = null
            requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData
        }
    }
    block()
}

/** The unified log (Console, `log stream`), where `Logger.SIMPLE`'s stdout doesn't reach. */
internal actual val PlatformHttpLogger: Logger = object : Logger {
    // The message is the format, escaped: Kotlin/Native doesn't bridge a String passed through NSLog's varargs.
    override fun log(message: String) = NSLog(message.replace("%", "%%"))
}
