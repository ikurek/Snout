package com.nightcode.snout.features.home

import androidx.compose.runtime.Composable
import com.nightcode.snout.features.activitylog.ActivityLogRoute
import com.nightcode.snout.features.analytics.AnalyticsRoute
import com.nightcode.snout.features.annotations.AnnotationsRoute
import com.nightcode.snout.features.dashboards.DashboardsRoute
import com.nightcode.snout.features.experiments.ExperimentsRoute
import com.nightcode.snout.features.featureflags.FeatureFlagsRoute
import com.nightcode.snout.features.insights.InsightsRoute
import com.nightcode.snout.shared.domain.model.appstate.HomeSection

/** Navigation callbacks a section may need; the shell and the standalone section screen both provide them. */
internal data class SectionNavigation(
    val onOpenDashboard: (dashboardId: Long) -> Unit,
    val onOpenInsight: (insightId: Long) -> Unit,
    val onOpenFeatureFlag: (flagId: Long) -> Unit,
    val onOpenExperiment: (experimentId: Long) -> Unit,
)

/** Body of a section without any chrome, so it renders the same inside the home tabs and as a pushed screen. */
@Composable
internal fun HomeSectionContent(section: HomeSection, navigation: SectionNavigation) {
    when (section) {
        HomeSection.Analytics -> AnalyticsRoute()
        HomeSection.Dashboards -> DashboardsRoute(onOpenDashboard = navigation.onOpenDashboard)
        HomeSection.Insights -> InsightsRoute(onOpenInsight = navigation.onOpenInsight)
        HomeSection.FeatureFlags -> FeatureFlagsRoute(onOpenFlag = navigation.onOpenFeatureFlag)
        HomeSection.Experiments -> ExperimentsRoute(onOpenExperiment = navigation.onOpenExperiment)
        HomeSection.Annotations -> AnnotationsRoute()
        HomeSection.ActivityLog -> ActivityLogRoute()
    }
}
