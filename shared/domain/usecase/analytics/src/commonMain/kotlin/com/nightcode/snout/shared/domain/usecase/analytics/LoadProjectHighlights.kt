package com.nightcode.snout.shared.domain.usecase.analytics

import arrow.core.Either
import com.nightcode.snout.shared.domain.data.analytics.AnalyticsRepository
import com.nightcode.snout.shared.domain.data.analytics.HighlightsRepository
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.HighlightSource
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.common.DataError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

/**
 * Loads [HighlightMetric]s of a project, each with what loading it gave: a request per [HighlightSource] the metrics
 * need, all at once, so a source that fails (revenue analytics PostHog refuses, say) leaves the others standing.
 */
@Single
class LoadProjectHighlights(
    private val analyticsRepository: AnalyticsRepository,
    private val highlightsRepository: HighlightsRepository,
) {
    suspend operator fun invoke(
        projectId: Long,
        range: AnalyticsRange,
        metrics: List<HighlightMetric>,
    ): Map<HighlightMetric, Either<DataError, Highlight>> = coroutineScope {
        val sources = metrics.map { it.source }.distinct()
        val loaded = sources.map { async { load(it, projectId, range) } }.awaitAll().let { sources.zip(it).toMap() }
        metrics.associateWith { metric -> loaded.getValue(metric.source).map { it[metric] ?: Highlight.None } }
    }

    private suspend fun load(
        source: HighlightSource,
        projectId: Long,
        range: AnalyticsRange,
    ): Either<DataError, Map<HighlightMetric, Highlight>> = when (source) {
        HighlightSource.WebOverview -> analyticsRepository.getOverview(projectId, range).map { overview ->
            overview.mapNotNull { metric -> metric.key.highlightMetric?.let { it to metric.toHighlight() } }.toMap()
        }

        HighlightSource.ActiveUsers -> highlightsRepository.getActiveUsers(projectId)

        HighlightSource.Revenue -> highlightsRepository.getRevenue(projectId)

        HighlightSource.AppVersions -> highlightsRepository.getLatestAppVersion(projectId)
            .map { mapOf(HighlightMetric.LatestAppVersion to it) }

        HighlightSource.Exceptions -> highlightsRepository.getExceptions(projectId, range)
    }
}

private val OverviewMetricKey.highlightMetric: HighlightMetric?
    get() = when (this) {
        OverviewMetricKey.Visitors -> HighlightMetric.Visitors
        OverviewMetricKey.Views -> HighlightMetric.Views
        OverviewMetricKey.Sessions -> HighlightMetric.Sessions
        OverviewMetricKey.SessionDuration -> HighlightMetric.SessionDuration
        OverviewMetricKey.BounceRate -> HighlightMetric.BounceRate
        else -> null
    }

private fun OverviewMetric.toHighlight(): Highlight =
    value?.let { Highlight.Figure(it, kind, changePercent, isIncreaseBad) } ?: Highlight.None
