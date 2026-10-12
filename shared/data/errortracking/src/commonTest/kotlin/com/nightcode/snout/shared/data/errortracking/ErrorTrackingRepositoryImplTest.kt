package com.nightcode.snout.shared.data.errortracking

import com.nightcode.snout.shared.api.errortracking.ErrorTrackingApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.emitsDuring
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatusFilter
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.jsonPrimitive

private const val ProjectId = 1L

private fun issuesJson(vararg ids: String) = ids.joinToString(",", """{"results":[""", "]}") { """{"id":"$it"}""" }

class ErrorTrackingRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = ErrorTrackingRepositoryImpl(ErrorTrackingApi(postHog.client, postHog.authDataStore))
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

        should("get the issues of the asked status") {
            repository.getIssues(ProjectId, IssueStatusFilter.Active).shouldBeRight().map { it.id } shouldBe
                listOf("a", "b")
            repository.getIssues(ProjectId, IssueStatusFilter.Resolved).shouldBeRight().map { it.id } shouldBe
                listOf("c")
        }

        should("return the new status and signal a change") {
            repository.changes.emitsDuring {
                repository.setStatus(ProjectId, "a", IssueStatusFilter.Resolved) shouldBeRight IssueStatus.Resolved
            } shouldBe true
        }

        should("not signal a change when the status change fails") {
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.changes.emitsDuring {
                repository.setStatus(ProjectId, "a", IssueStatusFilter.Resolved) shouldBeLeft DataError.Forbidden
            } shouldBe false
        }
    })
