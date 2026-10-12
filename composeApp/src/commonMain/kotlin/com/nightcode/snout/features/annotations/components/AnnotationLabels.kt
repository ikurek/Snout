package com.nightcode.snout.features.annotations.components

import com.nightcode.snout.shared.domain.model.annotations.AnnotationScope
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.annotation_scope_dashboard
import snout.composeapp.generated.resources.annotation_scope_insight
import snout.composeapp.generated.resources.annotation_scope_organization
import snout.composeapp.generated.resources.annotation_scope_project

/** `null` for a scope Snout does not know, which then goes untagged. */
internal val AnnotationScope.label: StringResource?
    get() = when (this) {
        AnnotationScope.Project -> Res.string.annotation_scope_project
        AnnotationScope.Organization -> Res.string.annotation_scope_organization
        AnnotationScope.Dashboard -> Res.string.annotation_scope_dashboard
        AnnotationScope.Insight -> Res.string.annotation_scope_insight
        AnnotationScope.Unknown -> null
    }
