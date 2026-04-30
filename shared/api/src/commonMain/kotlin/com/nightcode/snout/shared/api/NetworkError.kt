package com.nightcode.snout.shared.api

sealed interface NetworkError {

    val message: String

    data class Api(
        val statusCode: Int,
        val type: String,
        val code: String,
        val detail: String,
        val attr: String?
    ) : NetworkError {
        override val message: String = "API Error - $type: $code - $detail"
    }

    data class Http(val statusCode: Int) : NetworkError {
        override val message: String = "HTTP Error - $statusCode"
    }

    data object NoConnection : NetworkError {
        override val message: String = "Connection Error"
    }

    data class Unexpected(val cause: Throwable) : NetworkError {
        override val message: String = "Unexpected Error: ${cause.message}"
    }
}
