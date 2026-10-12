package com.nightcode.snout.shared.data.featureflags

import com.nightcode.snout.shared.api.featureflags.FeatureFlagsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.data.testutils.testJsonCacheStore
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first

private const val ProjectId = 1L

private fun flagJson(id: Long, active: Boolean, deleted: Boolean = false) =
    """{"id":$id,"key":"flag-$id","active":$active,"deleted":$deleted,"filters":{"groups":[]}}"""

class FeatureFlagRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = FeatureFlagRepositoryImpl(
            FeatureFlagsApi(postHog.client, postHog.authDataStore),
            testJsonCacheStore(),
        )
        postHog.onRequest { request ->
            when (request.method) {
                HttpMethod.Get -> respondJson(
                    """{"results":[${flagJson(1, true)},${flagJson(2, true)},${flagJson(3, true, deleted = true)}]}""",
                )

                HttpMethod.Patch -> respondJson(flagJson(2, active = false))

                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }

        suspend fun cachedFlags() = repository.observeFeatureFlags(ProjectId).first()

        should("leave deleted flags out of the cached list") {
            repository.refreshFeatureFlags(ProjectId).shouldBeRight()

            cachedFlags().shouldNotBeNull().map { it.id } shouldBe listOf(1L, 2L)
        }

        should("replace the toggled flag in the cached list") {
            repository.refreshFeatureFlags(ProjectId).shouldBeRight()

            repository.setActive(ProjectId, flagId = 2, active = false).shouldBeRight().active shouldBe false

            cachedFlags().shouldNotBeNull().map { it.id to it.active } shouldBe listOf(1L to true, 2L to false)
        }

        should("not create a cached list when none was loaded") {
            repository.setActive(ProjectId, flagId = 2, active = false).shouldBeRight()

            cachedFlags().shouldBeNull()
        }
    })
