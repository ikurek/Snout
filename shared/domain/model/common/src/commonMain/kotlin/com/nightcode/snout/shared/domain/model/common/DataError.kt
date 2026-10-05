package com.nightcode.snout.shared.domain.model.common

/** Error surfaced by every project-scoped data source. */
enum class DataError {
    Forbidden,

    /** PostHog answers 402 when the feature needs a paid plan. */
    PaymentRequired,
    NotFound,
    RateLimited,
    ConnectionError,
    UnexpectedError,
}
