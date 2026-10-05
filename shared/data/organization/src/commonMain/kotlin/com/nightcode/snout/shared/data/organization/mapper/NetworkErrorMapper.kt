package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError

internal fun NetworkError.toGetOrganizationsError(): GetOrganizationsError = when (this) {
    NetworkError.Api.Forbidden -> GetOrganizationsError.Forbidden
    NetworkError.Api.RateLimited -> GetOrganizationsError.RateLimited
    NetworkError.NoConnection -> GetOrganizationsError.ConnectionError
    else -> GetOrganizationsError.UnexpectedError
}
