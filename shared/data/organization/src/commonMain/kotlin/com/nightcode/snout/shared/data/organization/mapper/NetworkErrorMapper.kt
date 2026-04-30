package com.nightcode.snout.shared.data.organization.mapper

import com.nightcode.snout.shared.api.NetworkError
import com.nightcode.snout.shared.domain.model.organization.GetOrganizationsError
import io.ktor.http.HttpStatusCode

internal fun NetworkError.toGetOrganizationsError(): GetOrganizationsError = when (this) {
    is NetworkError.Api -> if (statusCode == HttpStatusCode.Unauthorized.value) {
        GetOrganizationsError.AuthorizationFailed
    } else {
        GetOrganizationsError.UnexpectedError
    }

    is NetworkError.Http -> if (statusCode == HttpStatusCode.Unauthorized.value) {
        GetOrganizationsError.AuthorizationFailed
    } else {
        GetOrganizationsError.UnexpectedError
    }

    is NetworkError.NoConnection -> GetOrganizationsError.ConnectionError

    is NetworkError.Unexpected -> GetOrganizationsError.UnexpectedError
}
