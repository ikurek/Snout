package com.nightcode.snout.shared.data.persons

import com.nightcode.snout.shared.api.persons.PersonsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.time.Instant

private const val ProjectId = 1L

class PersonRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = PersonRepositoryImpl(PersonsApi(postHog.client, postHog.authDataStore))

        should("find the person a distinct ID belongs to") {
            postHog.onRequest { respondJson("""{"results":[{"uuid":"p1","distinct_ids":["user-1"]}]}""") }

            repository.findPersonByDistinctId(ProjectId, "user-1").shouldBeRight().uuid shouldBe "p1"
        }

        should("report a distinct ID nobody has as not found") {
            postHog.onRequest { respondJson("""{"results":[]}""") }

            repository.findPersonByDistinctId(ProjectId, "nobody") shouldBeLeft DataError.NotFound
        }

        should("continue events before the given instant") {
            postHog.onRequest { respondJson("""{"results":[]}""") }

            repository.getEvents(ProjectId, "p1", Instant.parse("2026-10-01T10:00:00.123456Z")).shouldBeRight()

            val values = postHog.requests.single().jsonBody()
                .getValue("query").jsonObject
                .getValue("values").jsonObject
            values["before"] shouldBe JsonPrimitive("2026-10-01T10:00:00.123456Z")
        }

        should("pass a missing flag scope on as forbidden") {
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.getFlags(ProjectId, "user-1") shouldBeLeft DataError.Forbidden
        }
    })
