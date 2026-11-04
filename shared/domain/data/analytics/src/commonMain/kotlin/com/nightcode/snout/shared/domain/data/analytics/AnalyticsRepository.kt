package com.nightcode.snout.shared.domain.data.analytics

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.analytics.MetricTrend
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.WebBreakdown
import com.nightcode.snout.shared.domain.model.common.DataError

/** One call per part of the Analytics tab, so each can be shown as soon as it arrives. */
interface AnalyticsRepository {
    /** PostHog's web overview: visitors, views, sessions, duration, bounce rate and any it adds. */
    suspend fun getOverview(projectId: Long, range: AnalyticsRange): Either<DataError, List<OverviewMetric>>

    /** Weekly and monthly active users as of the end of [range], with the preceding period. */
    suspend fun getActiveUsers(projectId: Long, range: AnalyticsRange): Either<DataError, List<OverviewMetric>>

    /** Visitors, views and sessions over [range], each with the preceding period. */
    suspend fun getTrends(projectId: Long, range: AnalyticsRange): Either<DataError, List<MetricTrend>>

    suspend fun getBreakdown(
        projectId: Long,
        range: AnalyticsRange,
        breakdown: WebBreakdown,
    ): Either<DataError, BreakdownTable>
}
