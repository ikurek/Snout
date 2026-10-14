package com.nightcode.snout.shared.api

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import kotlin.concurrent.Volatile

/**
 * How much of each call debug builds log: the request line, the status and the headers. Bodies stay out by default
 * because they carry account and project data; switch to [LogLevel.BODY] or [LogLevel.ALL] while debugging locally.
 */
private val DebugLogLevel = LogLevel.HEADERS

/** Which kind of build the app is; only debug builds log their HTTP traffic. */
enum class AppBuild {
    Debug,
    Release,
}

/** The platform's console: logcat on Android, the unified log on iOS. */
internal expect val PlatformHttpLogger: Logger

/** Where [networkCall] logs why a call failed unexpectedly; set by [installDebugLogging], so unset in release. */
internal object DebugFailureLog {
    @Volatile
    var logger: Logger? = null
}

/**
 * Logs every call at [DebugLogLevel], with the personal API key (the bearer token) masked, and lets [networkCall] log
 * the causes of unexpected failures.
 */
internal fun HttpClientConfig<*>.installDebugLogging() {
    DebugFailureLog.logger = PlatformHttpLogger
    install(Logging) {
        logger = PlatformHttpLogger
        level = DebugLogLevel
        sanitizeHeader { it == HttpHeaders.Authorization }
    }
}
