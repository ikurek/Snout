package com.nightcode.snout.widgets

import arrow.core.Either
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.common.format.formatPercent
import com.nightcode.snout.shared.common.format.formatValue
import com.nightcode.snout.shared.common.format.trendChange
import com.nightcode.snout.shared.domain.model.insights.InsightDetail
import com.nightcode.snout.shared.domain.model.insights.InsightDisplay
import com.nightcode.snout.shared.domain.model.insights.InsightKind
import com.nightcode.snout.shared.domain.model.insights.InsightResult
import com.nightcode.snout.shared.domain.model.insights.InsightSummary
import com.nightcode.snout.shared.domain.model.insights.TrendSeries
import com.nightcode.snout.shared.domain.model.links.PostHogResource
import com.nightcode.snout.shared.domain.usecase.appstate.IsSignedIn
import com.nightcode.snout.shared.domain.usecase.insights.GetInsightDetail
import com.nightcode.snout.shared.domain.usecase.insights.GetInsights
import com.nightcode.snout.shared.domain.usecase.insights.SearchInsights
import com.nightcode.snout.shared.domain.usecase.links.GetPostHogUrl
import org.koin.core.annotation.Single

/** The most rows a ranking keeps; the widget shows as many as fit. */
private const val RankingRows = 5

private const val BreakdownSeparator = " · "

/** One saved insight reduced to what fits a widget. */
data class InsightWidgetContent(val name: String, val body: InsightWidgetBody, val link: String?)

sealed interface InsightWidgetBody {
    /**
     * One number: a line's or bars' latest point (with the line), a bold number's total (compared when the insight
     * compares), a funnel's conversion from its first step to its last.
     */
    data class Headline(
        val value: String,
        /** What [value] counts: the series' event, or the funnel's first and last step. */
        val label: String?,
        val change: TrendChange?,
        /** The first series' points for the sparkline; empty for numbers and funnels. */
        val series: List<Double>,
    ) : InsightWidgetBody

    /** Breakdown bars, pies and tables of series: the largest totals first. */
    data class Ranking(val rows: List<RankedValue>) : InsightWidgetBody

    /** Retention, paths, SQL tables and empty results: the widget offers to open the insight. */
    data object Unsupported : InsightWidgetBody
}

data class RankedValue(val label: String, val value: String)

/** An insight to pick for a widget. */
data class InsightChoice(val id: Long, val name: String)

@Single
class InsightWidgetLoader(
    private val isSignedIn: IsSignedIn,
    private val getInsightDetail: GetInsightDetail,
    private val getInsights: GetInsights,
    private val searchInsights: SearchInsights,
    private val getPostHogUrl: GetPostHogUrl,
) {
    /** Insight [insightId] of project [projectId]. */
    suspend fun load(projectId: Long, insightId: Long): WidgetState<InsightWidgetContent> =
        isSignedIn.loadWhenSignedIn {
            when (val detail = getInsightDetail(insightId, projectId)) {
                is Either.Left -> WidgetState.Failed(detail.value)

                is Either.Right -> {
                    val link = getPostHogUrl(PostHogResource.Insight(detail.value.summary.shortId), projectId)
                    WidgetState.Loaded(detail.value.toWidgetContent(link))
                }
            }
        }

    /**
     * [projectId]'s insights a widget can show, matching [query], or all of them (as the Insights tab lists them) for
     * a blank one.
     */
    suspend fun choices(projectId: Long, query: String): WidgetState<List<InsightChoice>> =
        isSignedIn.loadWhenSignedIn {
            val insights = if (query.isBlank()) getInsights(projectId) else searchInsights(query, projectId)
            when (insights) {
                is Either.Left -> WidgetState.Failed(insights.value)

                is Either.Right -> WidgetState.Loaded(
                    insights.value.filter { it.fitsWidget }.map { InsightChoice(it.id, it.name) },
                )
            }
        }
}

/** Whether a widget can show more than "open it in Snout" for this insight. */
val InsightSummary.fitsWidget: Boolean
    get() = kind in setOf(InsightKind.Trends, InsightKind.Funnels, InsightKind.Stickiness, InsightKind.Lifecycle) &&
        display != InsightDisplay.Unsupported

internal fun InsightDetail.toWidgetContent(link: String?): InsightWidgetContent =
    InsightWidgetContent(name = summary.name, body = widgetBody(), link = link)

/**
 * A line or bars show the last point under the line, since a total over a trend's range rarely means anything; a
 * number shows its total; breakdown bars, pies and tables rank their series; a funnel shows how many made it from the
 * first step through the last.
 */
private fun InsightDetail.widgetBody(): InsightWidgetBody = when (val result = result) {
    is InsightResult.Series -> {
        val first = result.series.firstOrNull()
        when {
            first == null -> InsightWidgetBody.Unsupported

            summary.display in setOf(InsightDisplay.BarValue, InsightDisplay.Pie, InsightDisplay.Table) ->
                InsightWidgetBody.Ranking(result.ranking())

            summary.display in setOf(InsightDisplay.Line, InsightDisplay.Bar) -> first.values.lastOrNull()
                ?.let { last ->
                    InsightWidgetBody.Headline(last.formatValue(result.format), first.eventLabel, null, first.values)
                }
                ?: InsightWidgetBody.Unsupported

            else -> InsightWidgetBody.Headline(
                value = first.total.formatValue(result.format),
                label = first.eventLabel,
                change = first.changePercent?.let { trendChange(it) },
                series = emptyList(),
            )
        }
    }

    is InsightResult.Funnel -> {
        val firstStep = result.steps.firstOrNull()
        val lastStep = result.steps.lastOrNull()
        if (firstStep == null || lastStep == null || result.steps.size < 2) {
            InsightWidgetBody.Unsupported
        } else {
            InsightWidgetBody.Headline(
                value = lastStep.conversionRate.formatPercent(isFraction = true),
                label = "${firstStep.name.eventLabel()} → ${lastStep.name.eventLabel()}",
                change = null,
                series = emptyList(),
            )
        }
    }

    is InsightResult.Retention, is InsightResult.Table, InsightResult.Unsupported, null -> InsightWidgetBody.Unsupported
}

/** The series by total, largest first, named by their breakdown value when they all break down one event. */
private fun InsightResult.Series.ranking(): List<RankedValue> {
    val event = series.first().label.substringBefore(BreakdownSeparator, missingDelimiterValue = "")
    val prefix = event + BreakdownSeparator
    val sharePrefix = prefix != BreakdownSeparator && series.all { it.label.startsWith(prefix) }
    return series.sortedByDescending { it.total }.take(RankingRows).map { item ->
        RankedValue(
            label = (if (sharePrefix) item.label.removePrefix(prefix) else item.label).eventLabel().ifEmpty { "–" },
            value = item.total.formatValue(format),
        )
    }
}

private val TrendSeries.eventLabel: String get() = label.eventLabel()

/** PostHog's own events as PostHog names them, wherever they appear: `$pageview` reads "Pageview". */
internal fun String.eventLabel(): String = PostHogEvent.replace(this) { match ->
    match.groupValues[1].replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
}

private val PostHogEvent = Regex("""\$([A-Za-z][A-Za-z0-9_]*)""")
