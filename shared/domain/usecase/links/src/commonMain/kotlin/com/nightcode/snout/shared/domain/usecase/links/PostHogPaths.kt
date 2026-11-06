package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.model.links.PostHogResource
import kotlin.uuid.Uuid

/*
 * The web app's page paths below `/project/{id}/`, in both directions. Kept side by side so a link Snout
 * builds always parses back to the same resource. Fingerprint pages are newer than PostHog's switch from
 * `error_tracking` to `error-tracking`, so only they use the new spelling.
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
    is PostHogResource.ErrorTrackingFingerprint -> "error-tracking/fingerprint/$fingerprint"
    PostHogResource.Logs -> "logs"
    PostHogResource.Persons -> "persons"
    is PostHogResource.Person -> "persons/$uuid"
    is PostHogResource.PersonByDistinctId -> "person/$distinctId"
    PostHogResource.Experiments -> "experiments"
    is PostHogResource.Experiment -> "experiments/$id"
    PostHogResource.Annotations -> "data-management/annotations"
    PostHogResource.ActivityLog -> "activity/explore"
}

/** Inverse of [path]: null for pages Snout has no screen for (replays, cohorts, "new" forms…). */
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

        // The web app moved to `error-tracking`; older instances and links still use `error_tracking`.
        "error_tracking", "error-tracking" -> parseErrorTracking(segments)

        "logs" -> PostHogResource.Logs.takeIf { segments.size == 1 }

        "persons" -> parseListOrItem(segments, PostHogResource.Persons) {
            id?.takeIf { Uuid.parseOrNull(it) != null }?.let(PostHogResource::Person)
        }

        "person" -> id?.takeIf { segments.size == 2 }?.let(PostHogResource::PersonByDistinctId)

        "experiments" -> parseListOrItem(segments, PostHogResource.Experiments) {
            id?.toLongOrNull()?.let(PostHogResource::Experiment)
        }

        "data-management" -> PostHogResource.Annotations.takeIf { segments == listOf("data-management", "annotations") }

        "activity" -> PostHogResource.ActivityLog.takeIf { segments == listOf("activity", "explore") }

        else -> null
    }
}

/** An issue by its id, or by an exception fingerprint as in PostHog's emails: `fingerprint/{percent-encoded}`. */
private fun parseErrorTracking(segments: List<String>): PostHogResource? {
    val fingerprint = segments.takeIf { it.size == 3 && it[1] == "fingerprint" }?.get(2)
    return if (fingerprint != null) {
        PostHogResource.ErrorTrackingFingerprint(fingerprint)
    } else {
        parseListOrItem(segments, PostHogResource.ErrorTracking) {
            segments.getOrNull(1)?.takeIf { Uuid.parseOrNull(it) != null }?.let(PostHogResource::ErrorTrackingIssue)
        }
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
