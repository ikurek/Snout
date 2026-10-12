package com.nightcode.snout.shared.domain.model.common

/** Error surfaced by every remote data source. */
enum class DataError {
    /** The personal API key was rejected (401). */
    Unauthorized,
    Forbidden,

    /** PostHog answers 402 when the feature needs a paid plan. */
    PaymentRequired,
    NotFound,
    RateLimited,
    ConnectionError,
    UnexpectedError,
}
