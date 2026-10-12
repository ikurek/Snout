package com.nightcode.snout.shared.data.errortracking

import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.data.testutils.testJsonCacheStore
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.jsonPrimitive

private const val ProjectId = 1L

private fun issuesJson(vararg ids: String) = ids.joinToString(",", """{"results":[""", "]}") { """{"id":"$it"}""" }

class ErrorTrackingRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = ErrorTrackingRepositoryImpl(
            ErrorTrackingApi(postHog.client, postHog.authDataStore),
            testJsonCacheStore(),
        )
        postHog.onRequest { request ->
            when (request.method) {
                HttpMethod.Post -> when (request.jsonBody().getValue("status").jsonPrimitive.content) {
                    "active" -> respondJson(issuesJson("a", "b"))
                    else -> respondJson(issuesJson("c"))
                }

                HttpMethod.Patch -> respondJson("""{"id":"a","status":"resolved"}""")

                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }

        suspend fun cachedIds(status: IssueStatusFilter) =
            repository.observeIssues(ProjectId, status).first()?.map { it.id }

        should("drop the issue from the cached list of its old status") {
            repository.refreshIssues(ProjectId, IssueStatusFilter.Active).shouldBeRight()
            repository.refreshIssues(ProjectId, IssueStatusFilter.Resolved).shouldBeRight()

            repository.setStatus(ProjectId, "a", IssueStatusFilter.Resolved) shouldBeRight IssueStatus.Resolved

            cachedIds(IssueStatusFilter.Active) shouldBe listOf("b")
            // It joins the resolved list on that list's next refresh, not before.
            cachedIds(IssueStatusFilter.Resolved) shouldBe listOf("c")
            cachedIds(IssueStatusFilter.Suppressed).shouldBeNull()
        }

        should("keep the cached lists when the status change fails") {
            repository.refreshIssues(ProjectId, IssueStatusFilter.Active).shouldBeRight()
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.setStatus(ProjectId, "a", IssueStatusFilter.Resolved) shouldBeLeft DataError.Forbidden

            cachedIds(IssueStatusFilter.Active).shouldNotBeNull() shouldBe listOf("a", "b")
        }
    })
