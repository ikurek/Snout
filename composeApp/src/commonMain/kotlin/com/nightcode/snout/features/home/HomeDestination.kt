package com.nightcode.snout.features.home

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.bottom_nav_analytics
import snout.composeapp.generated.resources.bottom_nav_dashboard
import snout.composeapp.generated.resources.ic_analytics_24dp
import snout.composeapp.generated.resources.ic_dashboard_24dp

internal enum class HomeDestination(val label: StringResource, val icon: DrawableResource) {
    Dashboard(Res.string.bottom_nav_dashboard, Res.drawable.ic_dashboard_24dp),
    Analytics(Res.string.bottom_nav_analytics, Res.drawable.ic_analytics_24dp),
}
