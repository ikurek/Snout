package com.nightcode.snout.shared.data.insights

import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.emitsDuring
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

private const val ProjectId = 1L

private fun insightJson(id: Long, favorited: Boolean) =
    """{"id":$id,"short_id":"s$id","name":"Insight $id","favorited":$favorited}"""

class InsightRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = InsightRepositoryImpl(InsightsApi(postHog.client, postHog.authDataStore))
        postHog.onRequest { request ->
            when (request.method) {
                HttpMethod.Get ->
                    respondJson("""{"results":[${insightJson(10, false)},${insightJson(11, false)}]}""")

                HttpMethod.Patch -> respondJson(insightJson(10, favorited = true))

                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }

        should("map the listed insights") {
            repository.getInsights(ProjectId).shouldBeRight().map { it.id to it.favorited } shouldBe
                listOf(10L to false, 11L to false)
        }

        should("return the favorited insight and signal a change") {
            repository.changes.emitsDuring {
                repository.setFavorite(ProjectId, insightId = 10, favorited = true).shouldBeRight().favorited shouldBe
                    true
            } shouldBe true
        }

        should("not signal a change when the update fails") {
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.changes.emitsDuring {
                repository.setFavorite(ProjectId, insightId = 10, favorited = true) shouldBeLeft DataError.Forbidden
            } shouldBe false
        }
    })
