package com.nightcode.snout.appwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import com.nightcode.snout.R
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.widgets.HighlightMissing
import com.nightcode.snout.widgets.HighlightTile
import com.nightcode.snout.widgets.HighlightsWidgetConfig
import com.nightcode.snout.widgets.HighlightsWidgetContent
import com.nightcode.snout.widgets.HighlightsWidgetLoader
import com.nightcode.snout.widgets.WidgetState
import org.koin.mp.KoinPlatform

/**
 * The project, range and metrics a widget shows, set in [HighlightsWidgetConfigurationActivity]; configuration only.
 * The metrics are their names, comma-separated, in the order the widget shows them.
 */
internal val HighlightsProjectKey = longPreferencesKey("project_id")
internal val HighlightsProjectNameKey = stringPreferencesKey("project_name")
internal val HighlightsRangeKey = stringPreferencesKey("range")
internal val HighlightsMetricsKey = stringPreferencesKey("metrics")

/** A tile's name, value and change, and the gap under it; one cell tall, its name and value. */
private val TileHeight = 58.dp
private val TileGap = 4.dp
private val CompactTileHeight = 36.dp

/** From these widths a widget has room for two tiles side by side, and three; below, one. */
private val TwoColumnWidth = 150.dp
private val ThreeColumnWidth = 330.dp

/** Null until the widget is set up. */
internal fun Preferences.highlightsConfig(): HighlightsWidgetConfig? = this[HighlightsProjectKey]?.let { projectId ->
    HighlightsWidgetConfig(
        projectId = projectId,
        range = this[HighlightsRangeKey]?.let { name -> AnalyticsRange.entries.firstOrNull { it.name == name } }
            ?: HighlightsWidgetConfig.DefaultRange,
        metrics = this[HighlightsMetricsKey]?.split(',')
            ?.mapNotNull { name -> HighlightMetric.entries.firstOrNull { it.name == name } }
            ?.takeIf { it.isNotEmpty() }
            ?: HighlightsWidgetConfig.DefaultMetrics,
        projectName = this[HighlightsProjectNameKey],
    )
}

/**
 * A project's highlights, picked per widget from its web stats, active users, revenue and app health: a grid of tiles,
 * two or three side by side and as many rows as fit, each a metric's value and its change over the widget's range.
 * Loads when its session starts (every 30 minutes, see `highlights_widget_info.xml`), and through [HighlightsResults]
 * when the account changes, the refresh button is tapped or its settings change; a tile whose request failed says so,
 * and the widget shows the error only when they all did.
 */
internal class HighlightsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override val previewSizeMode = PreviewSizes

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val configure = configureAction(
            context,
            HighlightsWidgetConfigurationActivity::class.java,
            GlanceAppWidgetManager(context).getAppWidgetId(id),
        )
        HighlightsResults.provide(context, id) { load -> HighlightsWidgetUi(load, configure) }
    }

    /** The widget picker's preview: this widget with sample numbers (see [publishWidgetPreviews]). */
    override suspend fun providePreview(context: Context, widgetCategory: Int) = provideContent {
        HighlightsWidgetUi(highlightsPreview(context), openInApp(context, link = null))
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) = HighlightsResults.forget(glanceId)
}

/** What each highlights widget loaded; nulls until the widget is set up. */
internal val HighlightsResults = WidgetResults(HighlightsWidget(), Preferences::highlightsConfig) { config ->
    config?.let { KoinPlatform.getKoin().get<HighlightsWidgetLoader>().load(it) }
}

class HighlightsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HighlightsWidget()
}

/** Loads the widget again, for its refresh button and a tap on an error. */
class RefreshHighlightsWidget : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) =
        HighlightsResults.reload(context, glanceId)
}

/** [load] holds nulls until the widget is set up. */
@Composable
internal fun HighlightsWidgetUi(
    load: WidgetLoad<HighlightsWidgetConfig?, WidgetState<HighlightsWidgetContent>?>,
    configure: Action,
) {
    val refresh = actionRunCallback<RefreshHighlightsWidget>()
    val config = load.config
    val state = load.result
    if (config == null || state == null) {
        return WidgetMessageFrame(
            title = string(R.string.widget_highlights_name),
            caption = null,
            message = string(R.string.widget_highlights_not_set_up),
            hint = null,
            onClick = configure,
            retry = refresh,
            load = load,
        )
    }
    val title = config.projectName ?: string(R.string.widget_highlights_name)
    val range = string(config.range.label)
    when (state) {
        is WidgetState.Loaded -> WidgetFrame(openInApp(LocalContext.current, state.content.link), padded = false) {
            Column(modifier = GlanceModifier.fillMaxSize().framePadding()) {
                HighlightsGrid(state.content.tiles, title, range, config.projectName, load, refresh)
            }
        }

        else -> WidgetNotice(state, title, range, load, refresh)
    }
}

/**
 * The header, then the tiles in rows of two or three (one on the narrowest), as many rows as fit, and the footer when
 * there is room left for it. One cell tall: the project and range on one line over a row of values.
 */
@Composable
private fun ColumnScope.HighlightsGrid(
    tiles: List<HighlightTile>,
    title: String,
    range: String,
    project: String?,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val size = LocalSize.current
    val compact = size.height < CompactHeight
    val columns = when {
        size.width >= ThreeColumnWidth -> 3
        size.width >= TwoColumnWidth -> 2
        else -> 1
    }
    if (compact) {
        CompactHeader(range, project, load, refresh)
        Spacer(modifier = GlanceModifier.defaultWeight())
        TileRow(tiles.take(columns), columns, compact = true)
        return
    }
    WidgetHeader(title, range, refresh, load.isReloading)
    // Another row of numbers before the footer: it only says when they loaded.
    val room = size.height - WidgetPadding * 2 - HeaderHeight.scaled()
    val rows = (room / TileHeight.scaled()).toInt().coerceAtLeast(1)
    val shown = tiles.chunked(columns).take(rows)
    Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp)) {
        shown.forEach { row -> TileRow(row, columns, compact = false) }
    }
    if (room - TileHeight.scaled() * shown.size >= FooterHeight.scaled()) {
        Spacer(modifier = GlanceModifier.defaultWeight())
        WidgetFooter(load.loadedAt)
    }
}

/** [tiles] side by side, each a [columns]th of the width, so a short last row lines up with those above. */
@Composable
private fun TileRow(tiles: List<HighlightTile>, columns: Int, compact: Boolean) {
    Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = if (compact) 0.dp else TileGap)) {
        tiles.forEach { HighlightTileView(it, compact, GlanceModifier.defaultWeight()) }
        repeat(columns - tiles.size) { Spacer(modifier = GlanceModifier.defaultWeight()) }
    }
}

/** A metric's name over its value, then its change, or why there's no value; one cell tall, the name and value. */
@Composable
private fun HighlightTileView(tile: HighlightTile, compact: Boolean, modifier: GlanceModifier) {
    val height = if (compact) CompactTileHeight else TileHeight - TileGap
    Column(modifier = modifier.padding(end = 8.dp).then(GlanceModifier.height(height.scaled()))) {
        WidgetCaption(string(tile.metric.tileLabel))
        WidgetValue(tile.value ?: "–", fontSize = if (compact) 18.sp else 20.sp)
        if (!compact) {
            val change = tile.change
            val missing = tile.missing
            when {
                change != null -> WidgetChangeText(change, fontSize = 11.sp)
                missing != null -> WidgetCaption(string(missing.label))
            }
        }
    }
}

private val HighlightMissing.label: Int
    get() = when (this) {
        HighlightMissing.NotSetUp -> R.string.widget_highlight_not_set_up
        HighlightMissing.Failed -> R.string.widget_highlight_failed
    }

/** A metric's name on its tile: short ("DAU"), where the settings spell it out. */
private val HighlightMetric.tileLabel: Int
    get() = when (this) {
        HighlightMetric.Visitors -> R.string.widget_tile_visitors
        HighlightMetric.Views -> R.string.widget_tile_views
        HighlightMetric.Sessions -> R.string.widget_tile_sessions
        HighlightMetric.SessionDuration -> R.string.widget_tile_duration
        HighlightMetric.BounceRate -> R.string.widget_tile_bounce_rate
        HighlightMetric.DailyActiveUsers -> R.string.widget_tile_dau
        HighlightMetric.WeeklyActiveUsers -> R.string.widget_tile_wau
        HighlightMetric.MonthlyActiveUsers -> R.string.widget_tile_mau
        HighlightMetric.Mrr -> R.string.widget_tile_mrr
        HighlightMetric.Revenue -> R.string.widget_tile_revenue
        HighlightMetric.LatestAppVersion -> R.string.widget_tile_version
        HighlightMetric.Exceptions -> R.string.widget_tile_exceptions
    }
