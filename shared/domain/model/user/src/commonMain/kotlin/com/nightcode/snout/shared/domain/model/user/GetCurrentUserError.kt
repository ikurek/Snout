package com.nightcode.snout.shared.domain.model.user

enum class GetCurrentUserError {
    Unauthorized,
    Forbidden,
    RateLimited,
    ConnectionError,
    UnexpectedError,
}
