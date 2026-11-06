package com.nightcode.snout.shared.api.persons.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class PersonsResponseDto(
    @SerialName("results") val results: List<PersonDto> = emptyList(),
    @SerialName("next") val next: String? = null,
)

@Serializable
data class PersonDto(
    @SerialName("uuid") val uuid: String,
    @SerialName("name") val name: String? = null,
    @SerialName("distinct_ids") val distinctIds: List<String> = emptyList(),
    @SerialName("properties") val properties: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_identified") val isIdentified: Boolean? = null,
)

@Serializable
data class FlagEvaluationDto(
    @SerialName("value") val value: JsonElement? = null,
    @SerialName("evaluation") val evaluation: FlagEvaluationReasonDto? = null,
)

@Serializable
data class FlagEvaluationReasonDto(
    @SerialName("reason") val reason: String? = null,
    /** Zero-based index of the release condition that decided it, when one did. */
    @SerialName("condition_index") val conditionIndex: Int? = null,
)
