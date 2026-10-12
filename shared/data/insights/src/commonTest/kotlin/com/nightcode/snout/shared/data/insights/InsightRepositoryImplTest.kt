package com.nightcode.snout.shared.data.insights

import com.nightcode.snout.shared.api.insights.InsightsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.data.testutils.testJsonCacheStore
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first

private const val ProjectId = 1L

private fun insightJson(id: Long, favorited: Boolean) =
    """{"id":$id,"short_id":"s$id","name":"Insight $id","favorited":$favorited}"""

class InsightRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = InsightRepositoryImpl(InsightsApi(postHog.client, postHog.authDataStore), testJsonCacheStore())
        postHog.onRequest { request ->
            when (request.method) {
                HttpMethod.Get ->
                    respondJson("""{"results":[${insightJson(10, false)},${insightJson(11, false)}]}""")

                HttpMethod.Patch -> respondJson(insightJson(10, favorited = true))

                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }

        suspend fun cachedInsights() = repository.observeInsights(ProjectId).first()

        should("replace the favorited insight in the cached list") {
            repository.refreshInsights(ProjectId).shouldBeRight()

            repository.setFavorite(ProjectId, insightId = 10, favorited = true).shouldBeRight().favorited shouldBe true

            cachedInsights().shouldNotBeNull().map { it.id to it.favorited } shouldBe listOf(10L to true, 11L to false)
        }

        should("not create a cached list when none was loaded") {
            repository.setFavorite(ProjectId, insightId = 10, favorited = true).shouldBeRight()

            cachedInsights().shouldBeNull()
        }

        should("leave the cached list alone when the update fails") {
            repository.refreshInsights(ProjectId).shouldBeRight()
            val before = cachedInsights()
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.setFavorite(ProjectId, insightId = 10, favorited = true) shouldBeLeft DataError.Forbidden

            cachedInsights() shouldBe before
        }
    })
