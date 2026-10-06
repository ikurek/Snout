package com.nightcode.snout.features.analytics

import androidx.compose.runtime.Composable
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.analytics_breakdown_country
import snout.composeapp.generated.resources.analytics_breakdown_device
import snout.composeapp.generated.resources.analytics_breakdown_pages
import snout.composeapp.generated.resources.analytics_breakdown_screens
import snout.composeapp.generated.resources.analytics_breakdown_sources
import snout.composeapp.generated.resources.analytics_metric_bounce_rate
import snout.composeapp.generated.resources.analytics_metric_other
import snout.composeapp.generated.resources.analytics_metric_session_duration
import snout.composeapp.generated.resources.analytics_metric_sessions
import snout.composeapp.generated.resources.analytics_metric_views
import snout.composeapp.generated.resources.analytics_metric_visitors
import snout.composeapp.generated.resources.analytics_range_30d
import snout.composeapp.generated.resources.analytics_range_7d
import snout.composeapp.generated.resources.analytics_range_90d
import snout.composeapp.generated.resources.analytics_range_today

/** Labels shared by the Analytics tab and the analytics settings screen. */
@Composable
internal fun AnalyticsRange.label(): String = stringResource(
    when (this) {
        AnalyticsRange.Today -> Res.string.analytics_range_today
        AnalyticsRange.Last7Days -> Res.string.analytics_range_7d
        AnalyticsRange.Last30Days -> Res.string.analytics_range_30d
        AnalyticsRange.Last90Days -> Res.string.analytics_range_90d
    },
)

@Composable
internal fun OverviewMetricKey.label(): String = stringResource(
    when (this) {
        OverviewMetricKey.Visitors -> Res.string.analytics_metric_visitors
        OverviewMetricKey.Views -> Res.string.analytics_metric_views
        OverviewMetricKey.Sessions -> Res.string.analytics_metric_sessions
        OverviewMetricKey.SessionDuration -> Res.string.analytics_metric_session_duration
        OverviewMetricKey.BounceRate -> Res.string.analytics_metric_bounce_rate
        OverviewMetricKey.Other -> Res.string.analytics_metric_other
    },
)

/** A metric PostHog added that Snout does not know keeps the label the API sent. */
@Composable
internal fun OverviewMetric.label(): String =
    if (key == OverviewMetricKey.Other) label.replaceFirstChar { it.uppercase() } else key.label()

@Composable
internal fun WebBreakdown.label(): String = stringResource(
    when (this) {
        WebBreakdown.Page -> Res.string.analytics_breakdown_pages
        WebBreakdown.Screen -> Res.string.analytics_breakdown_screens
        WebBreakdown.ReferringDomain -> Res.string.analytics_breakdown_sources
        WebBreakdown.Country -> Res.string.analytics_breakdown_country
        WebBreakdown.DeviceType -> Res.string.analytics_breakdown_device
    },
)
