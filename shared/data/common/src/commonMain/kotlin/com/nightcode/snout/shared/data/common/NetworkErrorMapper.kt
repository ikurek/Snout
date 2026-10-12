package com.nightcode.snout.shared.data.common

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.domain.model.common.DataError

fun NetworkError.toDataError(): DataError = when (this) {
    NetworkError.Api.Unauthorized -> DataError.Unauthorized
    NetworkError.Api.Forbidden -> DataError.Forbidden
    NetworkError.Api.PaymentRequired -> DataError.PaymentRequired
    NetworkError.Api.NotFound -> DataError.NotFound
    NetworkError.Api.RateLimited -> DataError.RateLimited
    NetworkError.NoConnection -> DataError.ConnectionError
    is NetworkError.Api.ServerError, is NetworkError.Api.Other, is NetworkError.Unexpected -> DataError.UnexpectedError
}
