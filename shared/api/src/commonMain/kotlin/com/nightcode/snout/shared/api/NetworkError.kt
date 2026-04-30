package com.nightcode.snout.shared.api

sealed interface NetworkError {

    data object NoConnection : NetworkError

    data class Unexpected(val cause: Throwable) : NetworkError

    sealed interface Api : NetworkError {
        data object Unauthorized : Api
        data object Forbidden : Api
        data object NotFound : Api
        data object RateLimited : Api
        data class ServerError(val statusCode: Int) : Api
        data class Other(val statusCode: Int) : Api
    }
}
