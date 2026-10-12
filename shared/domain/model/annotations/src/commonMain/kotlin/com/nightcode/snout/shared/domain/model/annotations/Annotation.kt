package com.nightcode.snout.shared.domain.model.annotations

import com.nightcode.snout.shared.common.testdata.TestData
import kotlin.time.Instant

enum class AnnotationScope {
    Project,
    Organization,
    Dashboard,
    Insight,
    Unknown,
}

data class Annotation(
    val id: Long,
    val content: String,
    val dateMarker: Instant,
    val scope: AnnotationScope,
    val targetName: String?,
    val createdBy: String?,
    val createdAt: Instant?,
)

@TestData
val TestAnnotation = Annotation(
    id = 5L,
    content = "Released mobile app 2.3.0",
    dateMarker = Instant.parse("2026-10-01T09:00:00Z"),
    scope = AnnotationScope.Project,
    targetName = null,
    createdBy = "Jane Doe",
    createdAt = Instant.parse("2026-10-01T09:05:00Z"),
)
