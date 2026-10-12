package com.nightcode.snout.shared.data.featureflags

import com.nightcode.snout.shared.api.featureflags.FeatureFlagsApi
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

private fun flagJson(id: Long, active: Boolean, deleted: Boolean = false) =
    """{"id":$id,"key":"flag-$id","active":$active,"deleted":$deleted,"filters":{"groups":[]}}"""

class FeatureFlagRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = FeatureFlagRepositoryImpl(FeatureFlagsApi(postHog.client, postHog.authDataStore))
        postHog.onRequest { request ->
            when (request.method) {
                HttpMethod.Get -> respondJson(
                    """{"results":[${flagJson(1, true)},${flagJson(2, true)},${flagJson(3, true, deleted = true)}]}""",
                )

                HttpMethod.Patch -> respondJson(flagJson(2, active = false))

                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }

        should("leave deleted flags out of the list") {
            repository.getFeatureFlags(ProjectId).shouldBeRight().map { it.id } shouldBe listOf(1L, 2L)
        }

        should("return the toggled flag and signal a change") {
            repository.changes.emitsDuring {
                repository.setActive(ProjectId, flagId = 2, active = false).shouldBeRight().active shouldBe false
            } shouldBe true
        }

        should("not signal a change when the toggle fails") {
            postHog.onRequest { respondError(HttpStatusCode.Forbidden) }

            repository.changes.emitsDuring {
                repository.setActive(ProjectId, flagId = 2, active = false) shouldBeLeft DataError.Forbidden
            } shouldBe false
        }
    })
