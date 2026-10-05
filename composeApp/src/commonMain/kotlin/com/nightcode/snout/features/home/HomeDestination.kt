package com.nightcode.snout.features.home

import com.nightcode.snout.shared.domain.model.appstate.PostHogResource
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.bottom_nav_analytics
import snout.composeapp.generated.resources.bottom_nav_dashboards
import snout.composeapp.generated.resources.bottom_nav_feature_flags
import snout.composeapp.generated.resources.bottom_nav_insights
import snout.composeapp.generated.resources.bottom_nav_more
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.ic_dashboard_24dp
import snout.composeapp.generated.resources.ic_flag_24dp
import snout.composeapp.generated.resources.ic_insights_24dp
import snout.composeapp.generated.resources.ic_more_horiz_24dp

internal enum class HomeDestination(
    val label: StringResource,
    val icon: DrawableResource,
    val resource: PostHogResource,
) {
    Analytics(Res.string.bottom_nav_analytics, Res.drawable.ic_analytics_24dp, PostHogResource.WebAnalytics),
    Dashboards(Res.string.bottom_nav_dashboards, Res.drawable.ic_dashboard_24dp, PostHogResource.Dashboards),
    Insights(Res.string.bottom_nav_insights, Res.drawable.ic_insights_24dp, PostHogResource.Insights),
    FeatureFlags(Res.string.bottom_nav_feature_flags, Res.drawable.ic_flag_24dp, PostHogResource.FeatureFlags),
    More(Res.string.bottom_nav_more, Res.drawable.ic_more_horiz_24dp, PostHogResource.Project),
}
