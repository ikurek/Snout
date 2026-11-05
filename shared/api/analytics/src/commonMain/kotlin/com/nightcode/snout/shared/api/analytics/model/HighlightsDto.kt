package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** SQL over PostHog's tables; its rows arrive in [QueryResponseDto.results], one array of columns each. */
@Serializable
data class HogQLQueryDto(@SerialName("query") val query: String) {
    @EncodeDefault
    @SerialName("kind")
    val kind: String = "HogQLQuery"
}

/** What the project highlights need of a project's settings: the currency PostHog reports its revenue in. */
@Serializable
data class ProjectSettingsDto(@SerialName("base_currency") val baseCurrency: String? = null)
