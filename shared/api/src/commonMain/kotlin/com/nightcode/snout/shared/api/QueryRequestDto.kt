package com.nightcode.snout.shared.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The body of PostHog's query endpoints (`query/` and the logs ones): the parameters wrapped in a `query` object. */
@Serializable
data class QueryRequestDto<T>(@SerialName("query") val query: T)

/** The `dateRange` of PostHog queries; dates are absolute or relative (`-7d`), a missing [dateTo] means now. */
@Serializable
data class DateRangeDto(
    @SerialName("date_from") val dateFrom: String,
    @SerialName("date_to") val dateTo: String? = null,
)

/** An event property filter, such as `$app_version` exactly one of [value]. */
@Serializable
data class PropertyFilterDto(
    @SerialName("type") val type: String,
    @SerialName("key") val key: String,
    @SerialName("operator") val operator: String,
    @SerialName("value") val value: List<String>,
)
