package com.nightcode.snout.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
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
