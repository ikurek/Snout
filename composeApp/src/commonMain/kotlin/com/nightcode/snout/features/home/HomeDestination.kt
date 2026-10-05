package com.nightcode.snout.features.home

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

internal enum class HomeDestination(val label: StringResource, val icon: DrawableResource) {
    Analytics(Res.string.bottom_nav_analytics, Res.drawable.ic_analytics_24dp),
    Dashboards(Res.string.bottom_nav_dashboards, Res.drawable.ic_dashboard_24dp),
    Insights(Res.string.bottom_nav_insights, Res.drawable.ic_insights_24dp),
    FeatureFlags(Res.string.bottom_nav_feature_flags, Res.drawable.ic_flag_24dp),
    More(Res.string.bottom_nav_more, Res.drawable.ic_more_horiz_24dp),
}
