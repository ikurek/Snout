package com.nightcode.snout.shared.api.featureflags

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.parseJson
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod

private const val ProjectId = 1L
private const val FlagId = 7L

/** Pins the request bodies sent to PostHog, so refactoring how they are built cannot change the wire format. */
class FeatureFlagsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = FeatureFlagsApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { respondJson("""{"id":$FlagId,"key":"flag"}""") }

        suspend fun shouldHavePatched(body: String) {
            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Patch
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/feature_flags/$FlagId/"
            request.jsonBody() shouldBe parseJson(body)
        }

        should("patch a flag active") {
            api.setActive(ProjectId, FlagId, active = true)

            shouldHavePatched("""{"active":true}""")
        }

        should("patch a flag inactive") {
            api.setActive(ProjectId, FlagId, active = false)

            shouldHavePatched("""{"active":false}""")
        }
    })
