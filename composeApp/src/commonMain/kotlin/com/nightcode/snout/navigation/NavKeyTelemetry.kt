package com.nightcode.snout.navigation

import com.nightcode.snout.shared.domain.model.telemetry.TelemetryScreen

/**
 * The screen a destination reports as, by kind only: a key's ids and names never leave the app. Null for Home and
 * onboarding, which report their tabs and steps themselves. Exhaustive, so a new destination can't be added without
 * deciding how it reports.
 */
internal val SnoutNavKey.telemetryScreen: TelemetryScreen?
    get() = when (this) {
        is OnboardingNavKey, HomeNavKey -> null
        is HomeSectionNavKey -> TelemetryScreen.of(section)
        SettingsNavKey -> TelemetryScreen.Settings
        NavigationSettingsNavKey -> TelemetryScreen.NavigationSettings
        AnalyticsSettingsNavKey -> TelemetryScreen.AnalyticsSettings
        PrivacySettingsNavKey -> TelemetryScreen.PrivacySettings
        LibrariesNavKey -> TelemetryScreen.Libraries
        is LibraryDetailNavKey -> TelemetryScreen.LibraryDetail
        is DashboardDetailNavKey -> TelemetryScreen.DashboardDetail
        is InsightDetailNavKey -> TelemetryScreen.InsightDetail
        is FeatureFlagDetailNavKey -> TelemetryScreen.FeatureFlagDetail
        is ErrorTrackingIssueNavKey -> TelemetryScreen.ErrorTrackingIssue
        is ExperimentDetailNavKey -> TelemetryScreen.ExperimentDetail
        is LogServiceNavKey -> TelemetryScreen.LogService
        is LogEntryNavKey -> TelemetryScreen.LogEntry
    }
