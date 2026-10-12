package com.nightcode.snout.navigation

import com.nightcode.snout.features.onboarding.OnboardingStep
import com.nightcode.snout.shared.domain.model.appstate.InitialLocation
import com.nightcode.snout.shared.domain.model.links.DeepLinkDestination

/* Where the domain's places (the first screen, a link's target) land in the app's back stack. */

internal fun InitialLocation.toNavKey(): SnoutNavKey = when (this) {
    InitialLocation.Intro -> OnboardingNavKey(OnboardingStep.Intro)
    InitialLocation.Login -> OnboardingNavKey(OnboardingStep.Login)
    InitialLocation.ProjectSelection -> OnboardingNavKey(OnboardingStep.ProjectSelection)
    InitialLocation.Home -> HomeNavKey
}

/** The screen to push on top of Home for [this]; null when Home itself is the destination. */
internal fun DeepLinkDestination.toNavKey(): SnoutNavKey? = when (this) {
    DeepLinkDestination.Home -> null
    is DeepLinkDestination.Section -> HomeSectionNavKey(section)
    is DeepLinkDestination.Dashboard -> DashboardDetailNavKey(id)
    is DeepLinkDestination.Insight -> InsightDetailNavKey(id)
    is DeepLinkDestination.FeatureFlag -> FeatureFlagDetailNavKey(id)
    is DeepLinkDestination.ErrorTrackingIssue -> ErrorTrackingIssueNavKey(id)
    is DeepLinkDestination.Experiment -> ExperimentDetailNavKey(id)
}
