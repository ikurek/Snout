package com.nightcode.snout.shared.data.common

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class NetworkErrorMapperTest :
    ShouldSpec({
        listOf(
            NetworkError.Api.Unauthorized to DataError.Unauthorized,
            NetworkError.Api.Forbidden to DataError.Forbidden,
            NetworkError.Api.PaymentRequired to DataError.PaymentRequired,
            NetworkError.Api.NotFound to DataError.NotFound,
            NetworkError.Api.RateLimited to DataError.RateLimited,
            NetworkError.NoConnection to DataError.ConnectionError,
            NetworkError.Api.ServerError(statusCode = 503) to DataError.UnexpectedError,
            NetworkError.Api.Other(statusCode = 418) to DataError.UnexpectedError,
            NetworkError.Unexpected(IllegalStateException()) to DataError.UnexpectedError,
        ).forEach { (networkError, dataError) ->
            should("map $networkError to $dataError") {
                networkError.toDataError() shouldBe dataError
            }
        }
    })
