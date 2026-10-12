package com.nightcode.snout.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** Sends [body] serialized as JSON. */
inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

suspend inline fun <reified T> HttpClient.postJson(url: String, body: T): HttpResponse = post(url) { jsonBody(body) }

suspend inline fun <reified T> HttpClient.patchJson(url: String, body: T): HttpResponse = patch(url) { jsonBody(body) }

/** The `dateRange` object of PostHog queries; dates are absolute or relative (`-7d`), an open end means now. */
fun JsonObjectBuilder.putDateRange(dateFrom: String, dateTo: String? = null) {
    putJsonObject("dateRange") {
        put("date_from", dateFrom)
        dateTo?.let { put("date_to", it) }
    }
}
