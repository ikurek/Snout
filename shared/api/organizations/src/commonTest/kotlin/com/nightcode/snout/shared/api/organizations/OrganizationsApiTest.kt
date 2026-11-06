package com.nightcode.snout.shared.api.organizations

import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import kotlin.uuid.Uuid

private val OrganizationId = Uuid.parse("00000000-0000-0000-0000-000000000001")

class OrganizationsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = OrganizationsApi(postHog.client, postHog.authDataStore)

        should("get an organization's members with their user ids") {
            postHog.onRequest {
                respondJson(
                    """{"results":[{"id":"m-1","user":{"id":7,"first_name":"Ada","email":"ada@example.com"}}]}""",
                )
            }

            api.getMembers(OrganizationId).shouldBeRight().results.single().user.id shouldBe 7L

            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Get
            request.url.toString() shouldBe "$TestInstanceUrl/api/organizations/$OrganizationId/members/?limit=200"
        }
    })
