package com.nightcode.snout.features.links

import androidx.navigation3.runtime.NavKey
import com.nightcode.snout.features.dashboards.detail.DashboardDetailNavKey
import com.nightcode.snout.features.errortracking.detail.ErrorTrackingIssueNavKey
import com.nightcode.snout.features.experiments.detail.ExperimentDetailNavKey
import com.nightcode.snout.features.featureflags.detail.FeatureFlagDetailNavKey
import com.nightcode.snout.features.home.SectionNavKey
import com.nightcode.snout.features.insights.detail.InsightDetailNavKey
import com.nightcode.snout.shared.domain.model.links.DeepLinkDestination

/** The screen to push on top of Home for [this]; null when Home itself is the destination. */
internal fun DeepLinkDestination.toNavKey(): NavKey? = when (this) {
    DeepLinkDestination.Home -> null
    is DeepLinkDestination.Section -> SectionNavKey(section)
    is DeepLinkDestination.Dashboard -> DashboardDetailNavKey(id)
    is DeepLinkDestination.Insight -> InsightDetailNavKey(id)
    is DeepLinkDestination.FeatureFlag -> FeatureFlagDetailNavKey(id)
    is DeepLinkDestination.ErrorTrackingIssue -> ErrorTrackingIssueNavKey(id)
    is DeepLinkDestination.Experiment -> ExperimentDetailNavKey(id)
}
