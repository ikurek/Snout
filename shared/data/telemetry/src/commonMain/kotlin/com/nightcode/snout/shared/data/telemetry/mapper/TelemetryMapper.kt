package com.nightcode.snout.shared.data.telemetry.mapper

import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.telemetry.TelemetryEvent
import com.nightcode.snout.shared.domain.model.telemetry.TelemetrySettings
import com.nightcode.snout.shared.storage.datastore.StoredTelemetrySettings

/** The event's name as the dashboards know it: snake_case, past tense. */
internal fun TelemetryEvent.toName(): String = when (this) {
    is TelemetryEvent.Onboarded -> "onboarded"
    is TelemetryEvent.DashboardViewed -> "dashboard_viewed"
    is TelemetryEvent.InsightViewed -> "insight_viewed"
    is TelemetryEvent.FeatureFlagToggled -> "feature_flag_toggled"
    is TelemetryEvent.IssueStatusChanged -> "issue_status_changed"
    TelemetryEvent.AnnotationCreated -> "annotation_created"
    is TelemetryEvent.LinkOpened -> "link_opened"
    is TelemetryEvent.ProjectSelected -> "project_selected"
    TelemetryEvent.LoggedOut -> "logged_out"
    is TelemetryEvent.ThemeChanged -> "theme_changed"
    is TelemetryEvent.NavigationChanged -> "navigation_changed"
    is TelemetryEvent.AnalyticsSettingsChanged -> "analytics_settings_changed"
}

/** What a dashboard filters and breaks down by: enums in snake_case, counts and switches, lists for several. */
internal fun TelemetryEvent.toProperties(): Map<String, Any> = when (this) {
    is TelemetryEvent.Onboarded -> mapOf("instance" to instance.wireName())

    is TelemetryEvent.DashboardViewed -> mapOf("tile_count" to tileCount)

    is TelemetryEvent.InsightViewed -> mapOf("kind" to kind.wireName())

    is TelemetryEvent.FeatureFlagToggled -> mapOf("active" to active)

    is TelemetryEvent.IssueStatusChanged -> mapOf("status" to status.wireName())

    is TelemetryEvent.LinkOpened -> mapOf("destination" to destination.wireName())

    is TelemetryEvent.ProjectSelected -> mapOf("source" to source.wireName())

    is TelemetryEvent.ThemeChanged -> mapOf("theme" to themeMode.wireName())

    is TelemetryEvent.NavigationChanged -> mapOf(
        "tabs" to tabs.map { it.wireName() },
        "landing" to landing.wireName(),
    )

    is TelemetryEvent.AnalyticsSettingsChanged -> preferences.properties()

    TelemetryEvent.AnnotationCreated, TelemetryEvent.LoggedOut -> emptyMap()
}

private fun AnalyticsPreferences.properties(): Map<String, Any> = mapOf(
    "default_range" to defaultRange.wireName(),
    "metrics" to metrics.map { it.wireName() }.sorted(),
    "show_trend_chart" to showTrendChart,
    "breakdowns" to breakdowns.map { it.wireName() }.sorted(),
)

/** A switch never set is on: reporting is on until the person turns it off. */
internal fun StoredTelemetrySettings.toTelemetrySettings() = TelemetrySettings(
    usage = usage ?: TelemetrySettings.Default.usage,
    errors = errors ?: TelemetrySettings.Default.errors,
)

/** `FeatureFlagDetail` -> `feature_flag_detail`, as the other wire names. */
internal fun Enum<*>.wireName(): String = name.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "_").lowercase()
