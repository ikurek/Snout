package com.nightcode.snout.shared.data.user.mapper

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError
import io.ktor.http.HttpStatusCode

internal fun NetworkError.toGetCurrentUserError(): GetCurrentUserError = when (this) {
    is NetworkError.Api -> if (statusCode == HttpStatusCode.Unauthorized.value) {
        GetCurrentUserError.AuthorizationFailed
    } else {
        GetCurrentUserError.UnexpectedError
    }

    is NetworkError.Http -> if (statusCode == HttpStatusCode.Unauthorized.value) {
        GetCurrentUserError.AuthorizationFailed
    } else {
        GetCurrentUserError.UnexpectedError
    }

    is NetworkError.NoConnection -> GetCurrentUserError.ConnectionError

    is NetworkError.Unexpected -> GetCurrentUserError.UnexpectedError
}
