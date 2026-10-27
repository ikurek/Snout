package com.nightcode.snout.shared.api

import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.telemetry.TrackNonFatal
import com.nightcode.snout.shared.telemetry.fake.FakeErrorTracker
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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

@OptIn(TestData::class)
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

        context("error reports") {
            val errorTracker = FakeErrorTracker()

            beforeTest {
                errorTracker.tracked.clear()
                FailureReport.trackNonFatal = TrackNonFatal(errorTracker)
            }
            afterTest { FailureReport.trackNonFatal = null }

            should("report an unexpected status with the endpoint, not the instance or ids") {
                val client = clientAnswering(HttpStatusCode.InternalServerError)

                networkCall { client.get("https://posthog.test/api/projects/42/insights/7/?refresh=true").bodyAsText() }

                errorTracker.tracked.single().message shouldBe "500 GET /api/projects/:id/insights/:id/"
            }

            should("report any other failure") {
                val failure = IllegalStateException("broken")

                networkCall<String> { throw failure }

                errorTracker.tracked.single().throwable shouldBe failure
            }

            should("not report what the app expects: errors it words, being offline, cancellation") {
                networkCall { clientAnswering(HttpStatusCode.NotFound).get(Url).bodyAsText() }
                networkCall { clientAnswering(HttpStatusCode.TooManyRequests).get(Url).bodyAsText() }
                networkCall { clientThrowing(IOException("offline")).get(Url).bodyAsText() }
                shouldThrow<CancellationException> { networkCall<String> { throw CancellationException("gone") } }

                errorTracker.tracked.shouldBeEmpty()
            }
        }

        should("redact a request path to the endpoint") {
            redactedApiPath("/api/projects/123/feature_flags/45/") shouldBe "/api/projects/:id/feature_flags/:id/"
            redactedApiPath("/api/users/@me/") shouldBe "/api/users/:id/"
            redactedApiPath("/acme/posthog/api/projects/1/error_tracking/issues/0190a2b4-7c1e/") shouldBe
                "/api/projects/:id/error_tracking/issues/:id/"
        }

        should("let cancellation through instead of reporting it as an error") {
            shouldThrow<CancellationException> {
                networkCall<String> { throw CancellationException("cancelled") }
            }
        }
    })
