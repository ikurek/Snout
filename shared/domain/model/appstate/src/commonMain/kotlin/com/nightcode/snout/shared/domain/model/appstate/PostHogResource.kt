package com.nightcode.snout.shared.domain.model.appstate

/** Something that has a page in the PostHog web app. */
sealed interface PostHogResource {
    data object Project : PostHogResource
    data object WebAnalytics : PostHogResource
    data object Dashboards : PostHogResource
    data class Dashboard(val id: Long) : PostHogResource
    data object Insights : PostHogResource
    data class Insight(val shortId: String) : PostHogResource
    data object FeatureFlags : PostHogResource
    data class FeatureFlag(val id: Long) : PostHogResource
    data object ErrorTracking : PostHogResource
    data class ErrorTrackingIssue(val id: String) : PostHogResource
    data object Experiments : PostHogResource
    data class Experiment(val id: Long) : PostHogResource
    data object Annotations : PostHogResource
    data object ActivityLog : PostHogResource
}
