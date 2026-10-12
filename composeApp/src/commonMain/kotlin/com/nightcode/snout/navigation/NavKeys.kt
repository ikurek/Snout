package com.nightcode.snout.navigation

import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.features.onboarding.OnboardingStep
import com.nightcode.snout.shared.domain.model.appstate.HomeSection
import com.nightcode.snout.shared.domain.model.logs.LogsRange
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/*
 * Every screen of the app. The keys live together because a sealed hierarchy must stay in one package: that is
 * what lets DefaultSavedStateConfiguration register all of them at once, so a new screen can't be forgotten there
 * and crash only when its back stack is saved.
 */

@Serializable
internal sealed interface SnoutNavKey : NavKey

/** [startStep] is where the pager opens: the intro after a logout, login after a 401, a project after a restart. */
@Serializable
internal data class OnboardingNavKey(val startStep: OnboardingStep) : SnoutNavKey

@Serializable
internal data object HomeNavKey : SnoutNavKey

/** A section opened from the More list: the same content as a tab, pushed with its own app bar. */
@Serializable
internal data class HomeSectionNavKey(val section: HomeSection) : SnoutNavKey

@Serializable
internal data object SettingsNavKey : SnoutNavKey

@Serializable
internal data object NavigationSettingsNavKey : SnoutNavKey

@Serializable
internal data object AnalyticsSettingsNavKey : SnoutNavKey

@Serializable
internal data class DashboardDetailNavKey(val dashboardId: Long) : SnoutNavKey

@Serializable
internal data class InsightDetailNavKey(val insightId: Long) : SnoutNavKey

@Serializable
internal data class FeatureFlagDetailNavKey(val flagId: Long) : SnoutNavKey

@Serializable
internal data class ErrorTrackingIssueNavKey(val issueId: String) : SnoutNavKey

@Serializable
internal data class ExperimentDetailNavKey(val experimentId: Long) : SnoutNavKey

/** A service's log lines over the [range] picked on the Logs screen. */
@Serializable
internal data class LogServiceNavKey(val service: String, val range: LogsRange) : SnoutNavKey

/** A log line; [service] and [timestamp] let it be found again, since PostHog can't fetch one line by [id]. */
@Serializable
internal data class LogEntryNavKey(val service: String, val id: String, val timestamp: Instant) : SnoutNavKey
