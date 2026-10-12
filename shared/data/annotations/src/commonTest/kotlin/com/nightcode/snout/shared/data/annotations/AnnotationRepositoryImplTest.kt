package com.nightcode.snout.shared.data.annotations

import com.nightcode.snout.shared.api.annotations.AnnotationsApi
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.respondError
import com.nightcode.snout.shared.data.testutils.respondJson
import com.nightcode.snout.shared.data.testutils.testJsonCacheStore
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlin.time.Instant

private const val ProjectId = 1L

private fun annotationJson(id: Long, day: Int?, deleted: Boolean = false) =
    """{"id":$id,"content":"Note $id","date_marker":${day?.let { "\"2026-10-0${it}T09:00:00Z\"" }},
    "scope":"project","deleted":$deleted}"""

class AnnotationRepositoryImplTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val repository = AnnotationRepositoryImpl(
            AnnotationsApi(postHog.client, postHog.authDataStore),
            testJsonCacheStore(),
        )
        var created = annotationJson(3, day = 3)
        postHog.onRequest { request ->
            when (request.method) {
                HttpMethod.Get -> respondJson(
                    """{"results":[${annotationJson(1, 1)},${annotationJson(2, 5)},${annotationJson(9, 4, true)}]}""",
                )

                HttpMethod.Post -> respondJson(created, HttpStatusCode.Created)

                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }
        val dateMarker = Instant.parse("2026-10-03T09:00:00Z")

        suspend fun cachedIds() = repository.observeAnnotations(ProjectId).first()?.map { it.id }

        should("cache annotations newest first without deleted ones") {
            repository.refreshAnnotations(ProjectId).shouldBeRight()

            cachedIds() shouldBe listOf(2L, 1L)
        }

        should("insert a created annotation into the cached list by date") {
            repository.refreshAnnotations(ProjectId).shouldBeRight()

            repository.createAnnotation(ProjectId, "Note 3", dateMarker).shouldBeRight().dateMarker shouldBe dateMarker

            cachedIds() shouldBe listOf(2L, 3L, 1L)
        }

        should("start the cached list when none was loaded") {
            repository.createAnnotation(ProjectId, "Note 3", dateMarker).shouldBeRight()

            cachedIds().shouldNotBeNull() shouldBe listOf(3L)
        }

        should("fail and cache nothing when the created annotation has no date") {
            repository.refreshAnnotations(ProjectId).shouldBeRight()
            created = annotationJson(3, day = null)

            repository.createAnnotation(ProjectId, "Note 3", dateMarker) shouldBeLeft DataError.UnexpectedError

            cachedIds() shouldBe listOf(2L, 1L)
        }
    })
