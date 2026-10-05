package com.nightcode.snout.shared.domain.model.organization

enum class GetOrganizationsError {
    Forbidden,
    RateLimited,
    ConnectionError,
    UnexpectedError,
}
