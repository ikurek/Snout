package com.nightcode.snout.shared.data.user.mapper

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.domain.model.user.GetCurrentUserError

internal fun NetworkError.toGetCurrentUserError(): GetCurrentUserError = when (this) {
    NetworkError.Api.Unauthorized -> GetCurrentUserError.Unauthorized
    NetworkError.Api.Forbidden -> GetCurrentUserError.Forbidden
    NetworkError.NoConnection -> GetCurrentUserError.ConnectionError
    else -> GetCurrentUserError.UnexpectedError
}
