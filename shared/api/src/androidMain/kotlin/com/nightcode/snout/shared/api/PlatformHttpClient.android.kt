package com.nightcode.snout.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger

/** OkHttp only caches responses when given a `Cache`, and none is configured. */
internal actual fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(OkHttp, block)

/** Logcat, split into lines that fit an entry. */
internal actual val PlatformHttpLogger: Logger = Logger.ANDROID
