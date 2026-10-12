package com.nightcode.snout.shared.data.annotations.mapper

import com.nightcode.snout.shared.api.annotations.model.AnnotationDto
import com.nightcode.snout.shared.data.common.nullIfBlank
import com.nightcode.snout.shared.domain.model.annotations.Annotation
import com.nightcode.snout.shared.domain.model.annotations.AnnotationScope

/** Returns null for entries that cannot be placed on a timeline. */
internal fun AnnotationDto.toAnnotation(): Annotation? {
    val marker = dateMarker ?: createdAt ?: return null
    return Annotation(
        id = id,
        content = content.orEmpty(),
        dateMarker = marker,
        scope = when (scope) {
            "project" -> AnnotationScope.Project
            "organization" -> AnnotationScope.Organization
            "dashboard" -> AnnotationScope.Dashboard
            "dashboard_item" -> AnnotationScope.Insight
            else -> AnnotationScope.Unknown
        },
        targetName = insightName.nullIfBlank() ?: dashboardName.nullIfBlank(),
        createdBy = createdBy?.displayName.nullIfBlank(),
        createdAt = createdAt,
    )
}
