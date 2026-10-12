package com.nightcode.snout.shared.api.insights

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.parseJson
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod

private const val ProjectId = 1L
private const val InsightId = 3L

/** Pins the request bodies sent to PostHog, so refactoring how they are built cannot change the wire format. */
class InsightsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = InsightsApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { respondJson("""{"id":$InsightId}""") }

        suspend fun shouldHavePatched(body: String) {
            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Patch
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/insights/$InsightId/"
            request.jsonBody() shouldBe parseJson(body)
        }

        should("patch an insight favorited") {
            api.setFavorite(ProjectId, InsightId, favorited = true)

            shouldHavePatched("""{"favorited":true}""")
        }

        should("patch an insight unfavorited") {
            api.setFavorite(ProjectId, InsightId, favorited = false)

            shouldHavePatched("""{"favorited":false}""")
        }
    })
