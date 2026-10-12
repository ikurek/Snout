package com.nightcode.snout.shared.data.logs

import com.nightcode.snout.shared.api.logs.LogsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.data.testutils.testJsonCacheStore
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.logs.LogSeverityFilter
import com.nightcode.snout.shared.domain.model.logs.LogsRange
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
        val repository = LogRepositoryImpl(LogsApi(postHog.client, postHog.authDataStore), testJsonCacheStore())
        var pageIds = listOf("a", "b")
        postHog.onRequest { respondJson(logsJson(pageIds)) }

        suspend fun loadPage() = repository.getLogs(
            projectId = ProjectId,
            service = Service,
            range = LogsRange.LastHour,
            severity = LogSeverityFilter.All,
            query = "",
            cursor = null,
        ).shouldBeRight()

        suspend fun getLog(id: String) = repository.getLog(ProjectId, Service, id, Timestamp)

        should("open a line from a loaded page without another request") {
            loadPage()

            getLog("b").shouldBeRight().body shouldBe "line b"
            postHog.requests shouldHaveSize 1
        }

        should("look up an unknown line within a second of its timestamp") {
            getLog("b").shouldBeRight().body shouldBe "line b"

            val dateRange = postHog.requests.single().jsonBody()
                .getValue("query").jsonObject
                .getValue("dateRange").jsonObject
            dateRange["date_from"] shouldBe JsonPrimitive("2026-10-01T09:59:59Z")
            dateRange["date_to"] shouldBe JsonPrimitive("2026-10-01T10:00:01Z")
        }

        should("remember lines found by a lookup") {
            getLog("a").shouldBeRight()

            getLog("b").shouldBeRight()
            postHog.requests shouldHaveSize 1
        }

        should("report a line missing from its lookup window as not found") {
            getLog("z") shouldBeLeft DataError.NotFound
        }

        should("forget the oldest lines past a thousand") {
            pageIds = (0..1_000).map { "line-$it" }
            loadPage()

            getLog("line-1000").shouldBeRight()
            postHog.requests shouldHaveSize 1
            getLog("line-0").shouldBeRight()
            postHog.requests shouldHaveSize 2
        }
    })
