package com.nightcode.snout.shared.domain.usecase.analytics

import arrow.core.Either
import arrow.core.getOrElse
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPart
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsSnapshot
import com.nightcode.snout.shared.domain.model.analytics.BreakdownTable
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProjectId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

/** How a fetched part adds itself to the snapshot. */
private typealias PartUpdate = (AnalyticsSnapshot) -> AnalyticsSnapshot

/**
 * The selected project's figures for a range, fetching only what the preferences show and emitting a snapshot each
 * time one of its parts arrives: first with every part pending, last with none. The parts are fetched in parallel,
 * so the tab fills in as they come rather than after the slowest.
 *
 * The figures are [projectId]'s when given (a home-screen widget names its own project), the selected project's
 * otherwise. An optional part that fails is left out. The overview is required: when it fails the flow emits that error
 * and ends, dropping the requests still running.
 */
@Single
class LoadAnalytics(
    private val getSelectedProjectId: GetSelectedProjectId,
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(
        range: AnalyticsRange,
        preferences: AnalyticsPreferences,
        projectId: Long? = null,
    ): Flow<Either<DataError, AnalyticsSnapshot>> = channelFlow {
        val project = projectId ?: getSelectedProjectId()
        val parts = AnalyticsPart.shownBy(preferences)
        var snapshot = AnalyticsSnapshot.empty(range, parts)
        val lock = Mutex()
        send(Either.Right(snapshot))
        try {
            // A failed overview throws out of this scope, which cancels the other parts' requests with it.
            coroutineScope {
                parts.forEach { part ->
                    launch {
                        val update = fetch(part, project, range)
                        lock.withLock {
                            snapshot = update(snapshot).copy(pending = snapshot.pending - part)
                            send(Either.Right(snapshot))
                        }
                    }
                }
            }
        } catch (failure: OverviewFailed) {
            send(Either.Left(failure.error))
        }
    }

    /** Fetches [part]; throws [OverviewFailed] when it's the overview and that fails. */
    private suspend fun fetch(part: AnalyticsPart, projectId: Long, range: AnalyticsRange): PartUpdate = when (part) {
        AnalyticsPart.Overview -> {
            val metrics = repository.getOverview(projectId, range).getOrElse { throw OverviewFailed(it) }
            // Active users may have arrived first; they stay after the overview's own cards.
            update { it.copy(overview = metrics + it.overview) }
        }

        AnalyticsPart.ActiveUsers -> {
            val metrics = repository.getActiveUsers(projectId, range).getOrNull().orEmpty()
            update { it.copy(overview = it.overview + metrics) }
        }

        AnalyticsPart.Trends -> {
            val trends = repository.getTrends(projectId, range).getOrNull().orEmpty()
            update { it.copy(trends = trends) }
        }

        is AnalyticsPart.Breakdown -> {
            val table = repository.getBreakdown(projectId, range, part.breakdown).getOrNull()
            update { it.copy(breakdowns = it.breakdowns.adding(table)) }
        }
    }

    private fun update(update: PartUpdate): PartUpdate = update

    /** Kept in display order, whatever order the tables arrive in. */
    private fun List<BreakdownTable>.adding(table: BreakdownTable?): List<BreakdownTable> =
        (this + listOfNotNull(table)).sortedBy { it.breakdown }

    private class OverviewFailed(val error: DataError) : Exception()
}
