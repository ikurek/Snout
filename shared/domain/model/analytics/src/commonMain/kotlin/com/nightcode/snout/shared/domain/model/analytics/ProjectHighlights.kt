package com.nightcode.snout.shared.domain.model.analytics

/** Where PostHog keeps a highlight: one request each, made only for the highlights that need it. */
enum class HighlightSource {
    /** PostHog's web overview, as the Analytics tab shows it. */
    WebOverview,

    /** Daily, weekly and monthly active users, and how many of the month's come back daily. */
    ActiveUsers,

    /** Revenue analytics: MRR and revenue to date, in the project's currency, and the customers behind them. */
    Revenue,

    /** The app versions events carry. */
    AppVersions,

    /** Error tracking's exceptions, and the users who ran into them. */
    Exceptions,
}

/**
 * A figure a project highlights widget can show, in the order its settings list them. Widgets keep their picks by
 * these names, so a metric is never renamed.
 */
enum class HighlightMetric(val source: HighlightSource) {
    Visitors(HighlightSource.WebOverview),
    Views(HighlightSource.WebOverview),
    Sessions(HighlightSource.WebOverview),
    SessionDuration(HighlightSource.WebOverview),
    BounceRate(HighlightSource.WebOverview),
    DailyActiveUsers(HighlightSource.ActiveUsers),
    WeeklyActiveUsers(HighlightSource.ActiveUsers),
    MonthlyActiveUsers(HighlightSource.ActiveUsers),

    /** Daily over monthly active users: how many of the month's users come back on a given day. */
    Stickiness(HighlightSource.ActiveUsers),
    Mrr(HighlightSource.Revenue),
    Revenue(HighlightSource.Revenue),

    /** Customers paying a subscription now: those with MRR. */
    Subscribers(HighlightSource.Revenue),

    /** Customers who ever paid. */
    PayingCustomers(HighlightSource.Revenue),
    LatestAppVersion(HighlightSource.AppVersions),
    Exceptions(HighlightSource.Exceptions),

    /** Users who ran into an exception over the range. */
    UsersWithErrors(HighlightSource.Exceptions),
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

    /**
     * Revenue analytics is set up, but none of its revenue is recurring: PostHog counts MRR only from subscriptions,
     * which revenue events carry only once a subscription property is set for them.
     */
    data object NoSubscriptions : Highlight
}
