package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.common.format.format
import com.nightcode.snout.shared.common.format.formatMoney
import com.nightcode.snout.shared.common.format.trendChange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.Highlight
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.HighlightSource
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.usecase.analytics.LoadProjectHighlights
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import org.koin.core.annotation.Single

/**
 * What one project highlights widget is set up to show: its project, the range its figures cover and its metrics, in
 * the order it shows them. [projectName] is the project's name when it was picked, which the widget shows.
 */
data class HighlightsWidgetConfig(
    val projectId: Long,
    val range: AnalyticsRange,
    val metrics: List<HighlightMetric>,
    val projectName: String? = null,
) {
    companion object {
        /** The most a widget shows: three by three on the largest. */
        const val MaxMetrics = 9

        /** What a new widget's settings start on: who comes, how many stay, and whether the app holds up. */
        val DefaultMetrics = listOf(
            HighlightMetric.DailyActiveUsers,
            HighlightMetric.MonthlyActiveUsers,
            HighlightMetric.SessionDuration,
            HighlightMetric.Exceptions,
        )
        val DefaultRange = AnalyticsRange.Last7Days
    }
}

/** A project's highlights, one tile per metric of the widget, in its order. */
data class HighlightsWidgetContent(
    val range: AnalyticsRange,
    val tiles: List<HighlightTile>,
    /** The page the widget opens: the project's web analytics, which the app shows as its Analytics tab. */
    val link: String?,
)

data class HighlightTile(
    val metric: HighlightMetric,
    /** Null when there's nothing to show; [missing] says why. */
    val value: String?,
    val change: TrendChange?,
    val missing: HighlightMissing? = null,
)

/** Why a tile has no value. */
enum class HighlightMissing {
    /** The project's PostHog doesn't collect it: revenue analytics isn't set up, or no event carries a version. */
    NotSetUp,

    /** Revenue analytics is set up, but none of its revenue recurs, which MRR and subscribers count. */
    NoSubscriptions,

    /** Its request failed; the other tiles stand. */
    Failed,
}

@Single
class HighlightsWidgetLoader(
    private val isSignedIn: IsSignedIn,
    private val loadProjectHighlights: LoadProjectHighlights,
    private val getPostHogUrl: GetPostHogUrl,
) {
    /**
     * The widget's metrics, a request per source they need. Fails as a whole only when every request did, with the
     * first metric's error: a widget whose tiles all say "couldn't load" says it once, with what a tap can do about it.
     */
    suspend fun load(config: HighlightsWidgetConfig): WidgetState<HighlightsWidgetContent> =
        isSignedIn.loadWhenSignedIn {
            val loaded = loadProjectHighlights(config.projectId, config.range, config.metrics)
            val errors = loaded.values.mapNotNull { it.leftOrNull() }
            if (errors.isNotEmpty() && errors.size == loaded.size) {
                WidgetState.Failed(errors.first())
            } else {
                WidgetState.Loaded(
                    HighlightsWidgetContent(
                        range = config.range,
                        tiles = config.metrics.map { metric -> loaded.getValue(metric).toTile(metric) },
                        link = getPostHogUrl(PostHogResource.WebAnalytics, config.projectId),
                    ),
                )
            }
        }
}

private fun Either<DataError, Highlight>.toTile(metric: HighlightMetric): HighlightTile = fold(
    ifLeft = { HighlightTile(metric, value = null, change = null, missing = HighlightMissing.Failed) },
    ifRight = { highlight ->
        when (highlight) {
            is Highlight.Figure -> HighlightTile(
                metric = metric,
                value = highlight.kind.format(highlight.value),
                change = highlight.changePercent?.let { trendChange(it, lowerIsBetter = highlight.isIncreaseBad) },
            )

            is Highlight.Money -> HighlightTile(metric, formatMoney(highlight.amount, highlight.currency), null)

            is Highlight.Version -> HighlightTile(metric, highlight.name, null)

            Highlight.NoSubscriptions ->
                HighlightTile(metric, value = null, change = null, missing = HighlightMissing.NoSubscriptions)

            // A web stat or a count with nothing in it is still a value: there was none.
            Highlight.None -> when (metric.source) {
                HighlightSource.Revenue, HighlightSource.AppVersions ->
                    HighlightTile(metric, value = null, change = null, missing = HighlightMissing.NotSetUp)

                else -> HighlightTile(metric, value = "–", change = null)
            }
        }
    },
)
