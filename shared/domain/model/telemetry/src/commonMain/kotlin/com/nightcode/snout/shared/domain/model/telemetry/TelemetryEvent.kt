package com.nightcode.snout.shared.domain.model.telemetry

import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.appstate.ThemeMode
import com.nightcode.snout.shared.domain.model.errortracking.IssueStatus
import com.nightcode.snout.shared.domain.model.insights.InsightKind

/**
 * What the app reports about how it's used: what was looked at, changed or set up. Events carry only kinds, counts
 * and switches, never anything from the person's PostHog (names, keys, URLs, ids): the reports go to Snout's own
 * PostHog project, and the person's data must never travel there. The data layer picks each one's wire name.
 */
sealed interface TelemetryEvent {

    /** Onboarding finished: a project was picked on the [instance]. */
    data class Onboarded(val instance: InstanceKind) : TelemetryEvent

    data class DashboardViewed(val tileCount: Int) : TelemetryEvent

    data class InsightViewed(val kind: InsightKind) : TelemetryEvent

    data class FeatureFlagToggled(val active: Boolean) : TelemetryEvent

    data class IssueStatusChanged(val status: IssueStatus) : TelemetryEvent

    data object AnnotationCreated : TelemetryEvent

    /** An incoming link (opened, or shared to Snout) was handled: shown in the app or handed to the browser. */
    data class LinkOpened(val destination: LinkDestination) : TelemetryEvent

    /** A project was picked: at the end of onboarding, or later in a project switcher. */
    data class ProjectSelected(val source: ProjectSelectionSource) : TelemetryEvent

    data object LoggedOut : TelemetryEvent

    data class ThemeChanged(val themeMode: ThemeMode) : TelemetryEvent

    data class NavigationChanged(val tabs: List<HomeSection>, val landing: HomeSection) : TelemetryEvent

    data class AnalyticsSettingsChanged(val preferences: AnalyticsPreferences) : TelemetryEvent
}

/** Where a PostHog instance runs, without saying which one a self-hosted instance is. */
enum class InstanceKind {
    CloudUs,
    CloudEu,
    SelfHosted,
    ;

    companion object {
        fun of(instanceUrl: String): InstanceKind = when (instanceUrl.trimEnd('/').substringAfter("://")) {
            "us.posthog.com", "app.posthog.com" -> CloudUs
            "eu.posthog.com" -> CloudEu
            else -> SelfHosted
        }
    }
}

/**
 * Where a project was picked. Links never switch projects: one to another project opens in the browser, which is
 * [LinkDestination.Browser].
 */
enum class ProjectSelectionSource {
    Onboarding,
    Home,
    Settings,
}

/** What an incoming link led to. */
enum class LinkDestination {
    Home,
    Section,
    Dashboard,
    Insight,
    FeatureFlag,
    ErrorTrackingIssue,
    Experiment,
    Browser,
}
