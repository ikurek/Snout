package com.nightcode.snout.shared.domain.model.appstate

/** A top-level section of the app; four of them sit in the bottom bar, the rest are listed under More. */
enum class HomeSection {
    Analytics,
    Dashboards,
    Insights,
    FeatureFlags,
    ErrorTracking,
    Logs,
    Experiments,
    Annotations,
    ActivityLog,
}

/** Page opened by the "Open in PostHog" action while the section is on screen. */
val HomeSection.resource: PostHogResource
    get() = when (this) {
        HomeSection.Analytics -> PostHogResource.WebAnalytics
        HomeSection.Dashboards -> PostHogResource.Dashboards
        HomeSection.Insights -> PostHogResource.Insights
        HomeSection.FeatureFlags -> PostHogResource.FeatureFlags
        HomeSection.ErrorTracking -> PostHogResource.ErrorTracking
        HomeSection.Logs -> PostHogResource.Logs
        HomeSection.Experiments -> PostHogResource.Experiments
        HomeSection.Annotations -> PostHogResource.Annotations
        HomeSection.ActivityLog -> PostHogResource.ActivityLog
    }
