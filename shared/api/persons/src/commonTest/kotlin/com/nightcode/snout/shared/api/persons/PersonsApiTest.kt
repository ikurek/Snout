package com.nightcode.snout.shared.api.persons

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ProjectId = 1L
private const val PersonUuid = "0190f0a2-1c2d-7e3f-8a9b-0c1d2e3f4a5b"

class PersonsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = PersonsApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { request ->
            respondJson(if (request.url.encodedPath.endsWith("evaluation_reasons/")) "{}" else """{"results":[]}""")
        }

        fun lastUrl() = postHog.requests.single().url.toString()

        suspend fun lastQuery(): JsonObject {
            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/query/"
            return request.jsonBody().getValue("query").jsonObject
        }

        should("list the newest persons without a search") {
            api.getPersons(ProjectId, search = " ")

            lastUrl() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/persons/?limit=50"
        }

        should("search persons by the trimmed text") {
            api.getPersons(ProjectId, search = " jane@example.com ")

            lastUrl() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/persons/?search=jane%40example.com&limit=50"
        }

        should("look a person up by the decoded distinct ID of a link") {
            api.getPersonsByDistinctId(ProjectId, "user%2042")

            lastUrl() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/persons/?distinct_id=user+42"
        }

        should("fetch a person by uuid") {
            postHog.onRequest { respondJson("""{"uuid":"$PersonUuid"}""") }

            api.getPerson(ProjectId, PersonUuid)

            lastUrl() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/persons/$PersonUuid/"
        }

        should("ask how flags evaluate for a distinct ID") {
            api.getFlagEvaluations(ProjectId, "user-42")

            lastUrl() shouldBe
                "$TestInstanceUrl/api/projects/$ProjectId/feature_flags/evaluation_reasons/?distinct_id=user-42"
        }

        should("pass the person to the events query as a value, not in the SQL") {
            api.getEvents(ProjectId, PersonUuid, before = null)

            val query = lastQuery()
            query["kind"] shouldBe JsonPrimitive("HogQLQuery")
            query["values"] shouldBe JsonObject(mapOf("person_id" to JsonPrimitive(PersonUuid)))
            val sql = query.getValue("query").jsonPrimitive.content
            sql shouldContain "person_id = toUUID({person_id})"
            sql shouldNotContain PersonUuid
            sql shouldNotContain "{before}"
        }

        should("page events before the last one shown") {
            api.getEvents(ProjectId, PersonUuid, before = "2026-10-01T10:00:00.123456Z")

            val query = lastQuery()
            query["values"] shouldBe JsonObject(
                mapOf(
                    "person_id" to JsonPrimitive(PersonUuid),
                    "before" to JsonPrimitive("2026-10-01T10:00:00.123456Z"),
                ),
            )
            query.getValue("query").jsonPrimitive.content shouldContain "timestamp < toDateTime64({before}, 6, 'UTC')"
        }

        should("query only the person's exceptions") {
            api.getExceptions(ProjectId, PersonUuid)

            val query = lastQuery()
            query["values"] shouldBe JsonObject(mapOf("person_id" to JsonPrimitive(PersonUuid)))
            query.getValue("query").jsonPrimitive.content shouldContain "event = '\$exception'"
        }
    })
