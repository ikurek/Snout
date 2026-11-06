package com.nightcode.snout.shared.data.persons.mapper

import com.nightcode.snout.shared.api.persons.PersonEventsPageSize
import com.nightcode.snout.shared.api.persons.model.FlagEvaluationDto
import com.nightcode.snout.shared.api.persons.model.FlagEvaluationReasonDto
import com.nightcode.snout.shared.api.persons.model.HogQLResultsDto
import com.nightcode.snout.shared.api.persons.model.PersonDto
import com.nightcode.snout.shared.domain.model.persons.PersonEvent
import com.nightcode.snout.shared.domain.model.persons.PersonException
import com.nightcode.snout.shared.domain.model.persons.PersonFlag
import com.nightcode.snout.shared.domain.model.persons.PersonFlagReason
import com.nightcode.snout.shared.domain.model.persons.PersonFlagValue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Instant

private fun row(vararg values: String?): List<JsonElement> = values.map { it?.let(::JsonPrimitive) ?: JsonNull }

class PersonsMapperTest :
    ShouldSpec({
        context("person") {
            val dto = PersonDto(
                uuid = "p1",
                name = "jane@example.com",
                distinctIds = listOf("user-1", "anon-1"),
                properties = JsonObject(
                    mapOf(
                        "plan" to JsonPrimitive("team"),
                        "email" to JsonPrimitive("jane@example.com"),
                        "seats" to JsonPrimitive(4),
                        "tags" to JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))),
                        "cleared" to JsonNull,
                    ),
                ),
                createdAt = "2026-03-14T08:12:00Z",
            )

            should("read the person with properties as text, sorted by key") {
                val person = dto.toPerson()

                person.displayName shouldBe "jane@example.com"
                person.email shouldBe "jane@example.com"
                person.createdAt shouldBe Instant.parse("2026-03-14T08:12:00Z")
                person.primaryDistinctId shouldBe "user-1"
                person.properties.toList() shouldContainExactly listOf(
                    "cleared" to "null",
                    "email" to "jane@example.com",
                    "plan" to "team",
                    "seats" to "4",
                    "tags" to """["a","b"]""",
                )
            }

            should("name an anonymous person by their first distinct ID") {
                val person = dto.copy(name = " ", properties = JsonObject(emptyMap())).toPerson()

                person.displayName shouldBe "user-1"
                person.email shouldBe null
            }
        }

        context("events") {
            should("read rows, using the URL or else the screen name") {
                val page = HogQLResultsDto(
                    listOf(
                        row("e1", "\$pageview", "2026-11-06T01:16:00.714000+01:00", "https://a.test/x", null),
                        row("e2", "\$screen", "2026-11-06T00:10:00Z", null, "Settings"),
                        row("e3", "broken", "not a time", null, null),
                    ),
                ).toEventPage()

                page.events shouldContainExactly listOf(
                    PersonEvent("e1", "\$pageview", Instant.parse("2026-11-06T00:16:00.714Z"), "https://a.test/x"),
                    PersonEvent("e2", "\$screen", Instant.parse("2026-11-06T00:10:00Z"), "Settings"),
                )
                page.nextBefore shouldBe null
            }

            should("continue a full page from its last event") {
                val rows = List(PersonEventsPageSize) { index ->
                    row("e$index", "click", "2026-11-06T00:${(59 - index).toString().padStart(2, '0')}:00Z", null, null)
                }

                HogQLResultsDto(rows).toEventPage().nextBefore shouldBe Instant.parse("2026-11-06T00:10:00Z")
            }
        }

        context("flags") {
            should("read values and reasons, the flags the person gets first") {
                val flags = mapOf(
                    "zeta" to FlagEvaluationDto(JsonPrimitive(true), FlagEvaluationReasonDto("condition_match", 0)),
                    "beta" to FlagEvaluationDto(
                        JsonPrimitive(false),
                        FlagEvaluationReasonDto("out_of_rollout_bound", 1),
                    ),
                    "alpha" to FlagEvaluationDto(JsonPrimitive("test"), FlagEvaluationReasonDto("condition_match", 2)),
                    "off" to FlagEvaluationDto(JsonPrimitive(false), FlagEvaluationReasonDto("something_new")),
                    "missing" to FlagEvaluationDto(),
                ).toFlags()

                flags shouldContainExactly listOf(
                    PersonFlag("alpha", PersonFlagValue.Variant("test"), PersonFlagReason.ConditionMatch, 3),
                    PersonFlag("zeta", PersonFlagValue.Enabled, PersonFlagReason.ConditionMatch, 1),
                    PersonFlag("beta", PersonFlagValue.Disabled, PersonFlagReason.OutOfRollout, 2),
                    PersonFlag("missing", PersonFlagValue.Disabled, PersonFlagReason.Unknown, null),
                    PersonFlag("off", PersonFlagValue.Disabled, PersonFlagReason.Unknown, null),
                )
            }
        }

        context("exceptions") {
            should("take the outermost type and message of each chain") {
                val exceptions = HogQLResultsDto(
                    listOf(
                        row(
                            "x1",
                            "2026-11-06T11:23:58.815000+01:00",
                            "issue-1",
                            """["SdkInitializationException","MlKitException"]""",
                            """["MlKit error 18","Device RAM is too low"]""",
                        ),
                        row("x2", "2026-11-06T09:00:00Z", "", null, "Plain message"),
                    ),
                ).toExceptions()

                exceptions shouldContainExactly listOf(
                    PersonException(
                        id = "x1",
                        timestamp = Instant.parse("2026-11-06T10:23:58.815Z"),
                        issueId = "issue-1",
                        type = "SdkInitializationException",
                        message = "MlKit error 18",
                    ),
                    PersonException(
                        id = "x2",
                        timestamp = Instant.parse("2026-11-06T09:00:00Z"),
                        issueId = null,
                        type = null,
                        message = "Plain message",
                    ),
                )
            }
        }
    })
