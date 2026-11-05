package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.common.format.format
import com.nightcode.snout.shared.common.format.trendChange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsPreferences
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.domain.model.analytics.TrendMetric
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.usecase.analytics.LoadAnalytics
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

/**
 * What one analytics widget is set up to show: its project, the stat it leads with and the range it covers.
 * [projectName] is the project's name when it was picked, which the widget shows so it can be told apart.
 */
data class AnalyticsWidgetConfig(
    val projectId: Long,
    val metric: OverviewMetricKey,
    val range: AnalyticsRange,
    val projectName: String? = null,
) {
    companion object {
        /** The stats a widget can lead with, in the order its settings list them; wide widgets add the others. */
        val Metrics: List<OverviewMetricKey> = listOf(
            OverviewMetricKey.Visitors,
            OverviewMetricKey.Views,
            OverviewMetricKey.Sessions,
            OverviewMetricKey.SessionDuration,
            OverviewMetricKey.BounceRate,
        )

        /** What a new widget's settings start on. */
        val DefaultMetric = OverviewMetricKey.Visitors
        val DefaultRange = AnalyticsRange.Last7Days
    }
}

/**
 * The analytics overview for a widget's range, leading with its stat: PostHog's web analytics, which count a mobile
 * app's screens as they count a website's pages, as the Analytics tab does.
 */
data class AnalyticsWidgetContent(
    val range: AnalyticsRange,
    /** The widget's stat first, then the others PostHog returned, in [AnalyticsWidgetConfig.Metrics] order. */
    val stats: List<WidgetStat>,
    /** The widget's stat per interval of [range]; empty for stats PostHog has no trend of (duration, bounce rate). */
    val series: List<Double>,
    /** The page the widget opens: the instance's web analytics, which the app shows as its Analytics tab. */
    val link: String?,
)

data class WidgetStat(
    val key: OverviewMetricKey,
    /** "–" when PostHog has no value for the range. */
    val value: String,
    val change: TrendChange?,
)

@Single
class AnalyticsWidgetLoader(
    private val isSignedIn: IsSignedIn,
    private val loadAnalytics: LoadAnalytics,
    private val getPostHogUrl: GetPostHogUrl,
) {
    /** The overview, plus the trends when the widget's stat has one: two queries at most. */
    suspend fun load(config: AnalyticsWidgetConfig): WidgetState<AnalyticsWidgetContent> = isSignedIn.loadWhenSignedIn {
        val trendMetric = config.metric.trendMetric
        val preferences = AnalyticsPreferences(
            defaultRange = config.range,
            metrics = AnalyticsWidgetConfig.Metrics.toSet(),
            showTrendChart = trendMetric != null,
            breakdowns = emptySet(),
        )
        // The tab shows each part as it arrives; a widget draws once, so it waits for all of them.
        val loaded = loadAnalytics(config.range, preferences, config.projectId)
            .first { it.fold({ true }, { it.isComplete }) }
        when (val snapshot = loaded) {
            is Either.Left -> WidgetState.Failed(snapshot.value)

            is Either.Right -> {
                val order = listOf(config.metric) + (AnalyticsWidgetConfig.Metrics - config.metric)
                WidgetState.Loaded(
                    AnalyticsWidgetContent(
                        range = config.range,
                        stats = order.mapNotNull { key ->
                            snapshot.value.overview.firstOrNull { it.key == key }?.toWidgetStat()
                        },
                        series = trendMetric?.let { snapshot.value.trend(it) }
                            ?.current?.points?.map { it.value }.orEmpty(),
                        link = getPostHogUrl(PostHogResource.WebAnalytics, config.projectId),
                    ),
                )
            }
        }
    }
}

private val OverviewMetricKey.trendMetric: TrendMetric?
    get() = when (this) {
        OverviewMetricKey.Visitors -> TrendMetric.Visitors
        OverviewMetricKey.Views -> TrendMetric.Views
        OverviewMetricKey.Sessions -> TrendMetric.Sessions
        else -> null
    }

private fun OverviewMetric.toWidgetStat() = WidgetStat(
    key = key,
    value = value?.let { kind.format(it) } ?: "–",
    change = changePercent?.let { trendChange(it, lowerIsBetter = isIncreaseBad) },
)
