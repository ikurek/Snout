package com.nightcode.snout.shared.domain.model.persons

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class Person(
    val uuid: String,
    val displayName: String,
    val email: String?,
    val distinctIds: List<String>,
    val properties: Map<String, String>,
    val createdAt: Instant?,
) {
    val primaryDistinctId: String? get() = distinctIds.firstOrNull()
}

data class PersonEvent(val id: String, val name: String, val timestamp: Instant, val location: String?)

data class PersonEventPage(val events: List<PersonEvent>, val nextBefore: Instant?)

sealed interface PersonFlagValue {
    data object Enabled : PersonFlagValue

    data object Disabled : PersonFlagValue

    data class Variant(val key: String) : PersonFlagValue
}

enum class PersonFlagReason {
    ConditionMatch,

    NoConditionMatch,

    OutOfRollout,

    FlagDisabled,

    NoGroup,
    Unknown,
}

data class PersonFlag(
    val key: String,
    val value: PersonFlagValue,
    val reason: PersonFlagReason,
    /** One-based, as PostHog numbers release conditions; null when no single condition decided it. */
    val conditionIndex: Int?,
)

data class PersonException(
    val id: String,
    val timestamp: Instant,
    val issueId: String?,
    val type: String?,
    val message: String?,
)

private val TestNow = Instant.parse("2026-10-05T09:30:00Z")

@TestData
val TestPerson = Person(
    uuid = "0190f0a2-1c2d-7e3f-8a9b-0c1d2e3f4a5b",
    displayName = "jane@example.com",
    email = "jane@example.com",
    distinctIds = listOf("user-4821", "0190f0a1-77aa-7bbb-8ccc-9ddd0eee1fff"),
    properties = mapOf(
        "\$browser" to "Chrome",
        "\$geoip_country_name" to "Poland",
        "\$initial_referring_domain" to "news.ycombinator.com",
        "email" to "jane@example.com",
        "name" to "Jane Doe",
        "plan" to "team",
    ),
    createdAt = Instant.parse("2026-03-14T08:12:00Z"),
)

@TestData
val TestPersons = listOf(
    TestPerson,
    TestPerson.copy(
        uuid = "0190f0a2-1c2d-7e3f-8a9b-0c1d2e3f4a5c",
        displayName = "marek@example.com",
        email = "marek@example.com",
        distinctIds = listOf("user-977"),
        properties = mapOf("email" to "marek@example.com", "plan" to "free"),
        createdAt = Instant.parse("2026-09-30T17:40:00Z"),
    ),
    TestPerson.copy(
        uuid = "0190f0a2-1c2d-7e3f-8a9b-0c1d2e3f4a5d",
        displayName = "0190f0b7-2e1d-7c3b-9a8f-6e5d4c3b2a19",
        email = null,
        distinctIds = listOf("0190f0b7-2e1d-7c3b-9a8f-6e5d4c3b2a19"),
        properties = mapOf("\$browser" to "Safari"),
        createdAt = Instant.parse("2026-10-05T07:02:00Z"),
    ),
)

@TestData
val TestPersonEvents = listOf(
    PersonEvent("e1", "\$pageview", TestNow - 2.minutes, "https://app.example.com/billing"),
    PersonEvent("e2", "upgrade_clicked", TestNow - 3.minutes, "https://app.example.com/billing"),
    PersonEvent("e3", "\$exception", TestNow - 9.minutes, "https://app.example.com/checkout"),
    PersonEvent("e4", "\$screen", TestNow - 64.minutes, "SettingsScreen"),
    PersonEvent("e5", "\$identify", TestNow - 65.minutes, null),
)

@TestData
val TestPersonFlags = listOf(
    PersonFlag("new-checkout", PersonFlagValue.Enabled, PersonFlagReason.ConditionMatch, conditionIndex = 2),
    PersonFlag("pricing-test", PersonFlagValue.Variant("annual-first"), PersonFlagReason.ConditionMatch, 1),
    PersonFlag("dark-mode", PersonFlagValue.Disabled, PersonFlagReason.OutOfRollout, conditionIndex = 1),
    PersonFlag("legacy-export", PersonFlagValue.Disabled, PersonFlagReason.FlagDisabled, conditionIndex = null),
)

@TestData
val TestPersonExceptions = listOf(
    PersonException(
        id = "x1",
        timestamp = TestNow - 9.minutes,
        issueId = "0190f0c3-0000-7000-8000-000000000001",
        type = "TypeError",
        message = "Cannot read properties of undefined (reading 'total')",
    ),
    PersonException(
        id = "x2",
        timestamp = TestNow - 3_000.minutes,
        issueId = "0190f0c3-0000-7000-8000-000000000002",
        type = "ChunkLoadError",
        message = "Loading chunk 412 failed.",
    ),
)
