package com.nightcode.snout.shared.api

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException

@Suppress("SwallowedException", "TooGenericExceptionCaught")
suspend fun <T> networkCall(call: suspend () -> T): Either<NetworkError, T> = either {
    try {
        call()
    } catch (e: ClientRequestException) {
        raise(
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> NetworkError.Api.Unauthorized
                HttpStatusCode.Forbidden -> NetworkError.Api.Forbidden
                HttpStatusCode.NotFound -> NetworkError.Api.NotFound
                HttpStatusCode.TooManyRequests -> NetworkError.Api.RateLimited
                else -> NetworkError.Api.Other(e.response.status.value)
            },
        )
    } catch (e: ServerResponseException) {
        raise(NetworkError.Api.ServerError(e.response.status.value))
    } catch (_: IOException) {
        raise(NetworkError.NoConnection)
    } catch (e: Exception) {
        raise(NetworkError.Unexpected(e))
    }
}
