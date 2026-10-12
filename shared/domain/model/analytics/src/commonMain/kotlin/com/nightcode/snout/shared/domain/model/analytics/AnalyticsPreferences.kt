package com.nightcode.snout.shared.domain.model.analytics

/**
 * What the Analytics tab shows: the range it opens on, which stat cards are visible, whether the trend chart is
 * drawn, and which breakdown tables appear. Metrics PostHog adds that Snout does not know
 * ([OverviewMetricKey.Other]) are always shown. Only what is enabled gets queried.
 */
data class AnalyticsPreferences(
    val defaultRange: AnalyticsRange,
    val metrics: Set<OverviewMetricKey>,
    val showTrendChart: Boolean,
    val breakdowns: Set<WebBreakdown>,
) {
    fun withMetric(key: OverviewMetricKey, enabled: Boolean): AnalyticsPreferences =
        copy(metrics = if (enabled) metrics + key else metrics - key)

    fun withBreakdown(breakdown: WebBreakdown, enabled: Boolean): AnalyticsPreferences =
        copy(breakdowns = if (enabled) breakdowns + breakdown else breakdowns - breakdown)

    companion object {
        /** Stat cards a person can switch off; at least one of them must stay on. */
        val ConfigurableMetrics: List<OverviewMetricKey> = OverviewMetricKey.entries - OverviewMetricKey.Other

        val Default = AnalyticsPreferences(
            defaultRange = AnalyticsRange.Last7Days,
            metrics = ConfigurableMetrics.toSet(),
            showTrendChart = true,
            breakdowns = WebBreakdown.entries.toSet(),
        )
    }
}

/** The snapshot reduced to what [preferences] asks for; a snapshot loaded earlier may hold more than is now enabled. */
fun AnalyticsSnapshot.applying(preferences: AnalyticsPreferences): AnalyticsSnapshot = copy(
    overview = overview.filter { it.key == OverviewMetricKey.Other || it.key in preferences.metrics },
    trends = if (preferences.showTrendChart) trends else emptyList(),
    breakdowns = breakdowns.filter { it.breakdown in preferences.breakdowns },
)
