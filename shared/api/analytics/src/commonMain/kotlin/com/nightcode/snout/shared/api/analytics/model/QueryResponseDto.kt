package com.nightcode.snout.shared.api.analytics.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/** Envelope of `POST /query`; [results] shape depends on the query kind so callers decode it themselves. */
@Serializable
data class QueryResponseDto(val results: JsonElement = JsonNull, val columns: List<String>? = null)
