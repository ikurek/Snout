package com.nightcode.snout.shared.data.logs

import com.nightcode.snout.shared.api.logs.LogsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.time.Instant

private const val ProjectId = 1L
private const val Service = "api"
private val Timestamp = Instant.parse("2026-10-01T10:00:00Z")

private fun logsJson(ids: List<String>) = ids.joinToString(",", """{"results":[""", "]}") { id ->
    """{"uuid":"$id","timestamp":"$Timestamp","body":"line $id"}"""
}

class LogRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = LogRepositoryImpl(LogsApi(postHog.client, postHog.authDataStore))
        postHog.onRequest { respondJson(logsJson(listOf("a", "b"))) }

        suspend fun getLog(id: String) = repository.getLog(ProjectId, Service, id, Timestamp)

        should("look up a line within a second of its timestamp") {
            getLog("b").shouldBeRight().body shouldBe "line b"

            val dateRange = postHog.requests.single().jsonBody()
                .getValue("query").jsonObject
                .getValue("dateRange").jsonObject
            dateRange["date_from"] shouldBe JsonPrimitive("2026-10-01T09:59:59Z")
            dateRange["date_to"] shouldBe JsonPrimitive("2026-10-01T10:00:01Z")
        }

        should("fetch the line again each time it is opened") {
            getLog("a").shouldBeRight()
            getLog("a").shouldBeRight()

            postHog.requests shouldHaveSize 2
        }

        should("report a line missing from its lookup window as not found") {
            getLog("z") shouldBeLeft DataError.NotFound
        }
    })
