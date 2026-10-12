package com.nightcode.snout.shared.domain.model.links

import com.nightcode.snout.shared.domain.model.appstate.HomeSection

/** A screen of the app a PostHog web link leads to. */
sealed interface DeepLinkDestination {
    data object Home : DeepLinkDestination
    data class Section(val section: HomeSection) : DeepLinkDestination
    data class Dashboard(val id: Long) : DeepLinkDestination
    data class Insight(val id: Long) : DeepLinkDestination
    data class FeatureFlag(val id: Long) : DeepLinkDestination
    data class ErrorTrackingIssue(val id: String) : DeepLinkDestination
    data class Experiment(val id: Long) : DeepLinkDestination
}
