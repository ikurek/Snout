package com.nightcode.snout.shared.api

import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

private const val Url = "https://posthog.test/api/"

/** A client that, like the app's, turns error statuses into exceptions, answering every request with [status]. */
private fun clientAnswering(status: HttpStatusCode) = HttpClient(MockEngine { respond("body", status) }) {
    expectSuccess = true
}

private fun clientThrowing(exception: Throwable) = HttpClient(MockEngine { throw exception }) {
    expectSuccess = true
}

class NetworkCallTest :
    ShouldSpec({
        should("return the body of a successful response") {
            val client = clientAnswering(HttpStatusCode.OK)

            networkCall { client.get(Url).bodyAsText() } shouldBeRight "body"
        }

        listOf(
            HttpStatusCode.Unauthorized to DataError.Unauthorized,
            HttpStatusCode.PaymentRequired to DataError.PaymentRequired,
            HttpStatusCode.Forbidden to DataError.Forbidden,
            HttpStatusCode.NotFound to DataError.NotFound,
            HttpStatusCode.TooManyRequests to DataError.RateLimited,
            HttpStatusCode.BadRequest to DataError.UnexpectedError,
            HttpStatusCode.Conflict to DataError.UnexpectedError,
            HttpStatusCode.InternalServerError to DataError.UnexpectedError,
            HttpStatusCode.ServiceUnavailable to DataError.UnexpectedError,
        ).forEach { (status, error) ->
            should("map status ${status.value} to $error") {
                val client = clientAnswering(status)

                networkCall { client.get(Url).bodyAsText() } shouldBeLeft error
            }
        }

        should("map an I/O failure to a connection error") {
            val client = clientThrowing(IOException("offline"))

            networkCall { client.get(Url).bodyAsText() } shouldBeLeft DataError.ConnectionError
        }

        should("map any other failure to an unexpected error") {
            networkCall<String> { throw IllegalStateException("broken") } shouldBeLeft DataError.UnexpectedError
        }

        should("log the cause of unexpected failures only once debug logging is installed") {
            val logged = mutableListOf<String>()
            networkCall { clientAnswering(HttpStatusCode.ServiceUnavailable).get(Url).bodyAsText() }
            DebugFailureLog.logger = object : Logger {
                override fun log(message: String) {
                    logged += message
                }
            }
            try {
                networkCall { clientAnswering(HttpStatusCode.ServiceUnavailable).get(Url).bodyAsText() }
                networkCall<String> { throw IllegalStateException("broken") }
                networkCall { clientAnswering(HttpStatusCode.NotFound).get(Url).bodyAsText() }
            } finally {
                DebugFailureLog.logger = null
            }

            logged shouldHaveSize 2
            logged[0] shouldContain "503"
            logged[0] shouldContain "GET $Url"
            logged[1] shouldContain "broken"
        }

        should("let cancellation through instead of reporting it as an error") {
            shouldThrow<CancellationException> {
                networkCall<String> { throw CancellationException("cancelled") }
            }
        }
    })
