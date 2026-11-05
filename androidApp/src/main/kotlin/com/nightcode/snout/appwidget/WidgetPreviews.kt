package com.nightcode.snout.appwidget

import android.content.Context
import android.os.Build
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import com.nightcode.snout.R
import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.widgets.AnalyticsWidgetConfig
import com.nightcode.snout.widgets.AnalyticsWidgetContent
import com.nightcode.snout.widgets.HighlightTile
import com.nightcode.snout.widgets.HighlightsWidgetConfig
import com.nightcode.snout.widgets.HighlightsWidgetContent
import com.nightcode.snout.widgets.InsightWidgetBody
import com.nightcode.snout.widgets.InsightWidgetContent
import com.nightcode.snout.widgets.WidgetStat
import com.nightcode.snout.widgets.WidgetState

/** What the widget picker shows a widget at: its default 2×2, and 4×2. */
internal val PreviewSizes = SizeMode.Responsive(setOf(DpSize(180.dp, 190.dp), DpSize(370.dp, 190.dp)))

private val PreviewUp = TrendChange("↑ 12.8%", ChangeTone.Good)
private val PreviewSeries = listOf(1500.0, 1720.0, 1650.0, 1980.0, 1210.0, 1890.0, 2480.0, 2100.0, 2300.0, 2650.0)

/** Sample numbers for the analytics widget's preview, drawn by the widget itself. */
internal fun analyticsPreview(
    context: Context,
): WidgetLoad<AnalyticsWidgetConfig?, WidgetState<AnalyticsWidgetContent>?> = WidgetLoad(
    config = AnalyticsWidgetConfig(
        projectId = 0,
        metric = OverviewMetricKey.Visitors,
        range = AnalyticsRange.Last7Days,
        projectName = context.getString(R.string.widget_preview_project),
    ),
    result = WidgetState.Loaded(
        AnalyticsWidgetContent(
            range = AnalyticsRange.Last7Days,
            stats = listOf(
                WidgetStat(OverviewMetricKey.Visitors, "12.4k", PreviewUp),
                WidgetStat(OverviewMetricKey.Views, "48.2k", TrendChange("↓ 5.5%", ChangeTone.Bad)),
                WidgetStat(OverviewMetricKey.Sessions, "15k", TrendChange("↑ 0.1%", ChangeTone.Good)),
            ),
            series = PreviewSeries,
            link = null,
        ),
    ),
    loadedAt = System.currentTimeMillis(),
)

/** Sample numbers for the highlights widget's preview, drawn by the widget itself. */
internal fun highlightsPreview(
    context: Context,
): WidgetLoad<HighlightsWidgetConfig?, WidgetState<HighlightsWidgetContent>?> {
    val tiles = listOf(
        HighlightTile(HighlightMetric.DailyActiveUsers, "2,480", PreviewUp),
        HighlightTile(HighlightMetric.MonthlyActiveUsers, "41.2k", TrendChange("↑ 3.4%", ChangeTone.Good)),
        HighlightTile(HighlightMetric.SessionDuration, "2m 23s", TrendChange("↓ 1.2%", ChangeTone.Bad)),
        HighlightTile(HighlightMetric.Exceptions, "206", TrendChange("↓ 10%", ChangeTone.Good)),
        HighlightTile(HighlightMetric.Mrr, "$12.4k", TrendChange("→ 0%", ChangeTone.Neutral)),
        HighlightTile(HighlightMetric.LatestAppVersion, "3.3.0", null),
    )
    return WidgetLoad(
        config = HighlightsWidgetConfig(
            projectId = 0,
            range = AnalyticsRange.Last7Days,
            metrics = tiles.map { it.metric },
            projectName = context.getString(R.string.widget_preview_project),
        ),
        result = WidgetState.Loaded(HighlightsWidgetContent(AnalyticsRange.Last7Days, tiles, link = null)),
        loadedAt = System.currentTimeMillis(),
    )
}

/** Sample numbers for the insight widget's preview, drawn by the widget itself. */
internal fun insightPreview(context: Context): WidgetLoad<InsightPick?, InsightLoad> {
    val name = context.getString(R.string.widget_insight_preview_name)
    return WidgetLoad(
        config = InsightPick(0, 0, name, context.getString(R.string.widget_preview_project)),
        result = InsightLoad.Done(
            WidgetState.Loaded(
                InsightWidgetContent(
                    name = name,
                    body = InsightWidgetBody.Headline("2,480", null, PreviewUp, PreviewSeries),
                    link = null,
                ),
            ),
        ),
        loadedAt = System.currentTimeMillis(),
    )
}

/**
 * Hands the picker the previews the widgets draw ([AnalyticsWidget.providePreview]), so they look as the widgets
 * do. Android 15 and later only; earlier launchers show the app's icon. Android limits how often an app may, so a
 * call it turns down changes nothing: the previews from an earlier start stay.
 */
internal suspend fun publishWidgetPreviews(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
    val manager = GlanceAppWidgetManager(context)
    manager.setWidgetPreviews(AnalyticsWidgetReceiver::class)
    manager.setWidgetPreviews(InsightWidgetReceiver::class)
    manager.setWidgetPreviews(HighlightsWidgetReceiver::class)
}
