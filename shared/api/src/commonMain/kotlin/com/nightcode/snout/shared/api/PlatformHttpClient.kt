package com.nightcode.snout.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/** An [HttpClient] on the platform's engine, set up to keep no HTTP cache: responses hold account data. */
internal expect fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient
