package com.nightcode.snout.shared.data.annotations.mapper

import com.nightcode.snout.shared.api.annotations.model.AnnotationDto
import com.nightcode.snout.shared.data.testutils.TestJson
import com.nightcode.snout.shared.domain.model.annotations.AnnotationScope
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class AnnotationMapperTest :
    ShouldSpec({
        should("map scope, author and target") {
            val dto = TestJson.decodeFromString<AnnotationDto>(
                """{"id":5,"content":"Release","date_marker":"2026-10-01T09:00:00Z","scope":"dashboard_item",
                "insight_name":"Signups","created_by":{"first_name":"Jane","last_name":"Doe"},
                "created_at":"2026-10-01T09:05:00Z"}""",
            )
            val annotation = dto.toAnnotation()
            annotation?.scope shouldBe AnnotationScope.Insight
            annotation?.targetName shouldBe "Signups"
            annotation?.createdBy shouldBe "Jane Doe"
            annotation?.dateMarker shouldBe Instant.parse("2026-10-01T09:00:00Z")
        }

        should("drop entries without any date") {
            TestJson.decodeFromString<AnnotationDto>("""{"id":1,"content":"x"}""").toAnnotation() shouldBe null
        }
    })
