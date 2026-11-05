package com.nightcode.snout.shared.domain.model.analytics

/** Where PostHog keeps a highlight: one request each, made only for the highlights that need it. */
enum class HighlightSource {
    /** PostHog's web overview, as the Analytics tab shows it. */
    WebOverview,

    /** Daily, weekly and monthly active users. */
    ActiveUsers,

    /** Revenue analytics: MRR and revenue to date, in the project's reporting currency. */
    Revenue,

    /** The app versions events carry. */
    AppVersions,

    /** Error tracking's exceptions. */
    Exceptions,
}

/** A figure a project highlights widget can show, in the order its settings list them. */
enum class HighlightMetric(val source: HighlightSource) {
    Visitors(HighlightSource.WebOverview),
    Views(HighlightSource.WebOverview),
    Sessions(HighlightSource.WebOverview),
    SessionDuration(HighlightSource.WebOverview),
    BounceRate(HighlightSource.WebOverview),
    DailyActiveUsers(HighlightSource.ActiveUsers),
    WeeklyActiveUsers(HighlightSource.ActiveUsers),
    MonthlyActiveUsers(HighlightSource.ActiveUsers),
    Mrr(HighlightSource.Revenue),
    Revenue(HighlightSource.Revenue),
    LatestAppVersion(HighlightSource.AppVersions),
    Exceptions(HighlightSource.Exceptions),
}

/** What a highlight shows. */
sealed interface Highlight {
    /** A number written as [kind] says, with its change since the previous period when PostHog has one. */
    data class Figure(
        val value: Double,
        val kind: OverviewMetricKind,
        val changePercent: Double?,
        val isIncreaseBad: Boolean = false,
    ) : Highlight

    /** An amount in [currency], the ISO code PostHog reports the project's revenue in. */
    data class Money(val amount: Double, val currency: String) : Highlight

    /** The newest app version events carried lately. */
    data class Version(val name: String) : Highlight

    /** PostHog has nothing to show: revenue analytics isn't set up, or no event carried an app version. */
    data object None : Highlight
}
