package com.nightcode.snout.shared.api

import arrow.core.Either
import arrow.core.raise.either
import com.nightcode.snout.shared.domain.model.common.DataError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequest
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

/**
 * Runs [call] and turns its failures into a [DataError]. Failures the app has no wording for (an unexpected status,
 * a server error or any other exception) all become [DataError.UnexpectedError], so debug builds log what they were.
 */
@Suppress("SwallowedException", "TooGenericExceptionCaught")
suspend fun <T> networkCall(call: suspend () -> T): Either<DataError, T> = either {
    try {
        call()
    } catch (e: ClientRequestException) {
        raise(
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> DataError.Unauthorized
                HttpStatusCode.Forbidden -> DataError.Forbidden
                HttpStatusCode.PaymentRequired -> DataError.PaymentRequired
                HttpStatusCode.NotFound -> DataError.NotFound
                HttpStatusCode.TooManyRequests -> DataError.RateLimited
                else -> e.unexpected()
            },
        )
    } catch (e: ServerResponseException) {
        raise(e.unexpected())
    } catch (e: CancellationException) {
        throw e
    } catch (_: IOException) {
        raise(DataError.ConnectionError)
    } catch (e: Exception) {
        DebugFailureLog.logger?.log("Unexpected failure: ${e.stackTraceToString()}")
        raise(DataError.UnexpectedError)
    }
}

/** Logs the status and request line only: the message would add the response body, which may hold account data. */
private fun ResponseException.unexpected(): DataError {
    val request: HttpRequest = response.call.request
    DebugFailureLog.logger?.log("Unexpected status ${response.status} for ${request.method.value} ${request.url}")
    return DataError.UnexpectedError
}
