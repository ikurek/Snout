package com.nightcode.snout.shared.domain.data.analytics

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.common.DataError

/** The project highlights the Analytics tab doesn't load; [AnalyticsRepository.getOverview] has the web stats. */
interface HighlightsRepository {
    /** Daily (the last whole day), weekly and monthly active users, each compared with a week before. */
    suspend fun getActiveUsers(projectId: Long): Either<DataError, Map<HighlightMetric, Highlight>>

    /** MRR and revenue to date across every revenue source; [Highlight.None] when revenue analytics isn't set up. */
    suspend fun getRevenue(projectId: Long): Either<DataError, Map<HighlightMetric, Highlight>>

    /** The newest app version events carried in the last week; [Highlight.None] when none carried one. */
    suspend fun getLatestAppVersion(projectId: Long): Either<DataError, Highlight>

    /** Exceptions over [range], compared with the period before. */
    suspend fun getExceptions(projectId: Long, range: AnalyticsRange): Either<DataError, Highlight>
}
