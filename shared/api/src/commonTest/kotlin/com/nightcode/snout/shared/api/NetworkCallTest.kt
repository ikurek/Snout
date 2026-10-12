package com.nightcode.snout.shared.api

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
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
            HttpStatusCode.Unauthorized to NetworkError.Api.Unauthorized,
            HttpStatusCode.PaymentRequired to NetworkError.Api.PaymentRequired,
            HttpStatusCode.Forbidden to NetworkError.Api.Forbidden,
            HttpStatusCode.NotFound to NetworkError.Api.NotFound,
            HttpStatusCode.TooManyRequests to NetworkError.Api.RateLimited,
            HttpStatusCode.BadRequest to NetworkError.Api.Other(statusCode = 400),
            HttpStatusCode.Conflict to NetworkError.Api.Other(statusCode = 409),
            HttpStatusCode.InternalServerError to NetworkError.Api.ServerError(statusCode = 500),
            HttpStatusCode.ServiceUnavailable to NetworkError.Api.ServerError(statusCode = 503),
        ).forEach { (status, error) ->
            should("map status ${status.value} to $error") {
                val client = clientAnswering(status)

                networkCall { client.get(Url).bodyAsText() } shouldBeLeft error
            }
        }

        should("map an I/O failure to no connection") {
            val client = clientThrowing(IOException("offline"))

            networkCall { client.get(Url).bodyAsText() } shouldBeLeft NetworkError.NoConnection
        }

        should("map any other failure to unexpected with its cause") {
            val cause = IllegalStateException("broken")

            val error = networkCall<String> { throw cause }.shouldBeLeft()

            error.shouldBeInstanceOf<NetworkError.Unexpected>().cause shouldBe cause
        }

        should("let cancellation through instead of reporting it as an error") {
            shouldThrow<CancellationException> {
                networkCall<String> { throw CancellationException("cancelled") }
            }
        }
    })
