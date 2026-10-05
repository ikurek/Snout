package com.nightcode.snout.shared.domain.model.common

/** Error surfaced by every project-scoped data source. */
enum class DataError {
    Forbidden,
    NotFound,
    RateLimited,
    ConnectionError,
    UnexpectedError,
}
