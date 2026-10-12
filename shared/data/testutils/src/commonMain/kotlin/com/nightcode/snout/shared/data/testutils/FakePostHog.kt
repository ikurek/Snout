package com.nightcode.snout.shared.data.testutils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.storage.datastore.AuthDataStore
import com.nightcode.snout.shared.storage.securestore.InMemorySecureStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

const val TestInstanceUrl = "https://posthog.test"

/**
 * A PostHog instance faked with Ktor's [MockEngine]. Pass [client] and [authDataStore] to the real API classes and
 * answer their requests in [onRequest]; requests nobody expected fail the test.
 */
class FakePostHog {
    private var handler: MockRequestHandler = { request ->
        error("Unexpected request: ${request.method.value} ${request.url}")
    }

    private val engine = MockEngine { request -> handler.invoke(this, request) }

    /** Fails like the app's client: `expectSuccess` turns error statuses into the exceptions `networkCall` maps. */
    val client = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(TestJson) }
        install(HttpTimeout)
    }

    /** Signed in to [TestInstanceUrl]; mirrors the instance URL preference key of [AuthDataStore]. */
    @OptIn(TestData::class)
    val authDataStore = AuthDataStore(
        InMemoryDataStore(preferencesOf(stringPreferencesKey("instance_url") to TestInstanceUrl)),
        InMemorySecureStore(),
    )

    /** Requests sent so far, oldest first. */
    val requests: List<HttpRequestData> get() = engine.requestHistory

    fun onRequest(handler: MockRequestHandler) {
        this.handler = handler
    }
}

fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
    respond(body, status, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))

fun MockRequestHandleScope.respondError(status: HttpStatusCode): HttpResponseData = respondJson("{}", status)

/** The JSON object a request sent as its body. */
suspend fun HttpRequestData.jsonBody(): JsonObject =
    TestJson.parseToJsonElement(body.toByteArray().decodeToString()).jsonObject

/** Parses a JSON literal, so expected request bodies compare structurally with [jsonBody] (key order aside). */
fun parseJson(literal: String): JsonElement = TestJson.parseToJsonElement(literal)

private class InMemoryDataStore(initial: Preferences) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(state.value).also { state.value = it }
}
