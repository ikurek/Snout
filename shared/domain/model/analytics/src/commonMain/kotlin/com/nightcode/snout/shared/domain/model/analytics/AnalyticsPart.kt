package com.nightcode.snout.shared.domain.model.analytics

private val ActiveUserKeys = setOf(OverviewMetricKey.WeeklyActiveUsers, OverviewMetricKey.MonthlyActiveUsers)

/** One request behind the Analytics tab; each arrives on its own, so the tab fills in as they do. */
sealed interface AnalyticsPart {
    /** The stat cards PostHog's web overview returns; the one part a snapshot can't do without. */
    data object Overview : AnalyticsPart

    /** The weekly and monthly active users cards, from a trends query of their own. */
    data object ActiveUsers : AnalyticsPart

    /** The visitors, views and sessions chart. */
    data object Trends : AnalyticsPart

    data class Breakdown(val breakdown: WebBreakdown) : AnalyticsPart

    companion object {
        /** What [preferences] shows, in display order, so disabled cards and tables cost no requests. */
        fun shownBy(preferences: AnalyticsPreferences): List<AnalyticsPart> = buildList {
            add(Overview)
            if (preferences.metrics.any { it in ActiveUserKeys }) add(ActiveUsers)
            if (preferences.showTrendChart) add(Trends)
            WebBreakdown.entries.filter { it in preferences.breakdowns }.forEach { add(Breakdown(it)) }
        }
    }
}
