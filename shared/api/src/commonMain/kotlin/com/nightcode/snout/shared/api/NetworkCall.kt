package com.nightcode.snout.shared.api

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException

@Suppress("SwallowedException", "TooGenericExceptionCaught")
suspend fun <T> networkCall(call: suspend () -> T): Either<NetworkError, T> = either {
    try {
        call()
    } catch (e: ClientRequestException) {
        val apiError = runCatching { e.response.body<ApiErrorDto>() }.getOrNull()
        if (apiError != null) {
            raise(
                NetworkError.Api(
                    statusCode = e.response.status.value,
                    type = apiError.type,
                    code = apiError.code,
                    detail = apiError.detail,
                    attr = apiError.attr,
                ),
            )
        } else {
            raise(NetworkError.Http(statusCode = e.response.status.value))
        }
    } catch (e: ServerResponseException) {
        raise(NetworkError.Http(statusCode = e.response.status.value))
    } catch (_: IOException) {
        raise(NetworkError.NoConnection)
    } catch (e: Exception) {
        raise(NetworkError.Unexpected(e))
    }
}
