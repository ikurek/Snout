package com.nightcode.snout.shared.domain.usecase.appstate

import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import kotlin.uuid.Uuid

/*
 * The web app's page paths below `/project/{id}/`, in both directions. Kept side by side so a link Snout
 * builds always parses back to the same resource.
 */

// One flat branch per resource; splitting it would only hide the URL table.
@Suppress("CyclomaticComplexMethod")
internal fun PostHogResource.path(): String = when (this) {
    PostHogResource.Project -> ""
    PostHogResource.WebAnalytics -> "web"
    PostHogResource.Dashboards -> "dashboard"
    is PostHogResource.Dashboard -> "dashboard/$id"
    PostHogResource.Insights -> "insights"
    is PostHogResource.Insight -> "insights/$shortId"
    PostHogResource.FeatureFlags -> "feature_flags"
    is PostHogResource.FeatureFlag -> "feature_flags/$id"
    PostHogResource.ErrorTracking -> "error_tracking"
    is PostHogResource.ErrorTrackingIssue -> "error_tracking/$id"
    PostHogResource.Logs -> "logs"
    PostHogResource.Experiments -> "experiments"
    is PostHogResource.Experiment -> "experiments/$id"
    PostHogResource.Annotations -> "data-management/annotations"
    PostHogResource.ActivityLog -> "activity/explore"
}

/** Inverse of [path]: null for pages Snout has no screen for (replays, persons, "new" forms…). */
@Suppress("CyclomaticComplexMethod")
internal fun parsePath(segments: List<String>): PostHogResource? {
    val id = segments.getOrNull(1)
    return when (segments.firstOrNull()) {
        null -> PostHogResource.Project

        "web" -> PostHogResource.WebAnalytics.takeIf { segments.size == 1 }

        "dashboard" -> parseListOrItem(segments, PostHogResource.Dashboards) {
            id?.toLongOrNull()?.let(PostHogResource::Dashboard)
        }

        // The insight editor lives at `insights/{shortId}/edit`; it's the same insight.
        "insights" -> when {
            id == null -> PostHogResource.Insights
            id == "new" -> null
            segments.drop(2) in listOf(emptyList(), listOf("edit")) -> PostHogResource.Insight(id)
            else -> null
        }

        "feature_flags" -> parseListOrItem(segments, PostHogResource.FeatureFlags) {
            id?.toLongOrNull()?.let(PostHogResource::FeatureFlag)
        }

        "error_tracking" -> parseListOrItem(segments, PostHogResource.ErrorTracking) {
            id?.takeIf { Uuid.parseOrNull(it) != null }?.let(PostHogResource::ErrorTrackingIssue)
        }

        "logs" -> PostHogResource.Logs.takeIf { segments.size == 1 }

        "experiments" -> parseListOrItem(segments, PostHogResource.Experiments) {
            id?.toLongOrNull()?.let(PostHogResource::Experiment)
        }

        "data-management" -> PostHogResource.Annotations.takeIf { segments == listOf("data-management", "annotations") }

        "activity" -> PostHogResource.ActivityLog.takeIf { segments == listOf("activity", "explore") }

        else -> null
    }
}

private inline fun parseListOrItem(
    segments: List<String>,
    list: PostHogResource,
    item: () -> PostHogResource?,
): PostHogResource? = when (segments.size) {
    1 -> list
    2 -> item()
    else -> null
}
