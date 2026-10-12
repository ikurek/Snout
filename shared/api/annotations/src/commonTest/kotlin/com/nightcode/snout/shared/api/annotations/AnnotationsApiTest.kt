package com.nightcode.snout.shared.api.annotations

import com.nightcode.snout.shared.api.annotations.model.CreateAnnotationRequestDto
import com.nightcode.snout.shared.data.testutils.FakePostHog
import com.nightcode.snout.shared.data.testutils.TestInstanceUrl
import com.nightcode.snout.shared.data.testutils.jsonBody
import com.nightcode.snout.shared.data.testutils.parseJson
import com.nightcode.snout.shared.data.testutils.respondJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import kotlin.time.Instant

private const val ProjectId = 1L
private val DateMarker = Instant.parse("2026-10-01T10:00:00Z")

/** Pins the request bodies sent to PostHog, so refactoring how they are built cannot change the wire format. */
class AnnotationsApiTest :
    ShouldSpec({
        val postHog = FakePostHog()
        val api = AnnotationsApi(postHog.client, postHog.authDataStore)
        postHog.onRequest { respondJson("""{"id":5}""") }

        suspend fun shouldHavePosted(body: String) {
            val request = postHog.requests.single()
            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "$TestInstanceUrl/api/projects/$ProjectId/annotations/"
            request.jsonBody() shouldBe parseJson(body)
        }

        // The default scope equals the DTO default, so it is left out and PostHog applies its own default.
        should("leave the default scope out of a new annotation") {
            api.createAnnotation(
                ProjectId,
                CreateAnnotationRequestDto(content = "Release 2.1", dateMarker = DateMarker),
            )

            shouldHavePosted("""{"content":"Release 2.1","date_marker":"2026-10-01T10:00:00Z"}""")
        }

        should("send a non-default scope of a new annotation") {
            api.createAnnotation(
                ProjectId,
                CreateAnnotationRequestDto(content = "Release 2.1", dateMarker = DateMarker, scope = "organization"),
            )

            shouldHavePosted(
                """{"content":"Release 2.1","date_marker":"2026-10-01T10:00:00Z","scope":"organization"}""",
            )
        }
    })
