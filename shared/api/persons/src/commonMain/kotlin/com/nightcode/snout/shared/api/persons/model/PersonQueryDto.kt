package com.nightcode.snout.shared.api.persons.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * SQL over PostHog's tables. Anything that comes from outside the app goes in [values] and is named in the query as
 * a `{placeholder}`, so PostHog inserts it as a constant and it can never change the query itself.
 */
@Serializable
data class HogQLQueryDto(
    @SerialName("query") val query: String,
    @SerialName("values") val values: Map<String, String>? = null,
) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "HogQLQuery"
}

@Serializable
data class HogQLResultsDto(@SerialName("results") val results: List<List<JsonElement>> = emptyList())
