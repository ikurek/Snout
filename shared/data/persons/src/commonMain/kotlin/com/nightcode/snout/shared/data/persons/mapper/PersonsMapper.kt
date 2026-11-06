package com.nightcode.snout.shared.data.persons.mapper

import com.nightcode.snout.shared.api.persons.PersonEventsPageSize
import com.nightcode.snout.shared.api.persons.model.FlagEvaluationDto
import com.nightcode.snout.shared.api.persons.model.HogQLResultsDto
import com.nightcode.snout.shared.api.persons.model.PersonDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.data.common.string
import com.nightcode.snout.shared.domain.model.persons.Person
import com.nightcode.snout.shared.domain.model.persons.PersonEvent
import com.nightcode.snout.shared.domain.model.persons.PersonEventPage
import com.nightcode.snout.shared.domain.model.persons.PersonException
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import com.nightcode.snout.shared.domain.model.persons.PersonFlagReason
import com.nightcode.snout.shared.domain.model.persons.PersonFlagValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.time.Instant

private enum class EventColumn { Id, Name, Timestamp, Url, Screen }

private enum class ExceptionColumn { Id, Timestamp, IssueId, Types, Messages }

private operator fun List<JsonElement>.get(column: Enum<*>): JsonElement? = getOrNull(column.ordinal)

internal fun PersonDto.toPerson(): Person {
    val email = properties["email"].string().nullIfBlank()
    return Person(
        uuid = uuid,
        displayName = name.nullIfBlank() ?: email ?: distinctIds.firstOrNull() ?: uuid,
        email = email,
        distinctIds = distinctIds,
        properties = properties.entries.sortedBy { it.key }.associate { (key, value) -> key to value.displayText() },
        createdAt = createdAt?.toInstantOrNull(),
    )
}

private fun JsonElement.displayText(): String = when (this) {
    JsonNull -> "null"
    is JsonPrimitive -> content
    else -> toString()
}

internal fun HogQLResultsDto.toEventPage(): PersonEventPage {
    val events = results.mapNotNull { row ->
        PersonEvent(
            id = row[EventColumn.Id].string() ?: return@mapNotNull null,
            name = row[EventColumn.Name].string().orEmpty(),
            timestamp = row[EventColumn.Timestamp].string()?.toInstantOrNull() ?: return@mapNotNull null,
            location = row[EventColumn.Url].string().nullIfBlank() ?: row[EventColumn.Screen].string().nullIfBlank(),
        )
    }
    val nextBefore = events.lastOrNull()?.timestamp?.takeIf { results.size >= PersonEventsPageSize }
    return PersonEventPage(events, nextBefore)
}

internal fun Map<String, FlagEvaluationDto>.toFlags(): List<PersonFlag> = map { (key, dto) ->
    PersonFlag(
        key = key,
        value = dto.value.toFlagValue(),
        reason = dto.evaluation?.reason.toFlagReason(),
        conditionIndex = dto.evaluation?.conditionIndex?.plus(1),
    )
}.sortedWith(compareBy<PersonFlag> { it.value == PersonFlagValue.Disabled }.thenBy { it.key })

private fun JsonElement?.toFlagValue(): PersonFlagValue {
    val primitive = this as? JsonPrimitive ?: return PersonFlagValue.Disabled
    return when {
        primitive.isString -> primitive.content.nullIfBlank()?.let(PersonFlagValue::Variant) ?: PersonFlagValue.Disabled
        primitive.booleanOrNull == true -> PersonFlagValue.Enabled
        else -> PersonFlagValue.Disabled
    }
}

/** PostHog's `FeatureFlagMatchReason` codes; super and holdout conditions are conditions that matched too. */
private fun String?.toFlagReason(): PersonFlagReason = when (this) {
    "condition_match", "super_condition_value", "holdout_condition_value" -> PersonFlagReason.ConditionMatch
    "no_condition_match" -> PersonFlagReason.NoConditionMatch
    "out_of_rollout_bound" -> PersonFlagReason.OutOfRollout
    "disabled", "flag_disabled" -> PersonFlagReason.FlagDisabled
    "no_group_type" -> PersonFlagReason.NoGroup
    else -> PersonFlagReason.Unknown
}

internal fun HogQLResultsDto.toExceptions(): List<PersonException> = results.mapNotNull { row ->
    PersonException(
        id = row[ExceptionColumn.Id].string() ?: return@mapNotNull null,
        timestamp = row[ExceptionColumn.Timestamp].string()?.toInstantOrNull() ?: return@mapNotNull null,
        issueId = row[ExceptionColumn.IssueId].string().nullIfBlank(),
        type = row[ExceptionColumn.Types].firstOfList(),
        message = row[ExceptionColumn.Messages].firstOfList(),
    )
}

/**
 * The first item of a list property: HogQL hands property arrays over as JSON text, though a plain value or a real
 * array is read too. The outermost exception of a chain comes first.
 */
private fun JsonElement?.firstOfList(): String? {
    val array = when (this) {
        is JsonArray -> this

        is JsonPrimitive -> string()?.let { text ->
            runCatching { Json.parseToJsonElement(text) }.getOrNull() as? JsonArray ?: return text.nullIfBlank()
        }

        else -> null
    }
    return array?.firstOrNull().string().nullIfBlank()
}

private fun String.toInstantOrNull(): Instant? = runCatching { Instant.parse(this) }.getOrNull()
