package com.nightcode.snout.shared.domain.model.telemetry

import com.nightcode.snout.shared.domain.model.appstate.HomeSection

/** A screen of the app, as screen views report it: its kind only, never what it shows. */
enum class TelemetryScreen {
    Intro,
    Login,
    ProjectSelection,
    Analytics,
    Dashboards,
    Insights,
    FeatureFlags,
    ErrorTracking,
    Logs,
    Experiments,
    Annotations,
    ActivityLog,
    More,
    DashboardDetail,
    InsightDetail,
    FeatureFlagDetail,
    ErrorTrackingIssue,
    ExperimentDetail,
    LogService,
    LogEntry,
    Settings,
    NavigationSettings,
    AnalyticsSettings,
    PrivacySettings,
    Libraries,
    LibraryDetail,
    ;

    companion object {
        /** A section is the same screen as a Home tab and pushed from More. */
        fun of(section: HomeSection): TelemetryScreen = when (section) {
            HomeSection.Analytics -> Analytics
            HomeSection.Dashboards -> Dashboards
            HomeSection.Insights -> Insights
            HomeSection.FeatureFlags -> FeatureFlags
            HomeSection.ErrorTracking -> ErrorTracking
            HomeSection.Logs -> Logs
            HomeSection.Experiments -> Experiments
            HomeSection.Annotations -> Annotations
            HomeSection.ActivityLog -> ActivityLog
        }
    }
}
