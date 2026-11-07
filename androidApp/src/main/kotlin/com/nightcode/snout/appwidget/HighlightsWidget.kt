package com.nightcode.snout.appwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nightcode.snout.R
import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.TrendChange
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

/** Top and bottom inset of a widget one cell tall, whose tiles go without cards to fit. */
private val CompactVerticalPadding = 8.dp

/** Null until the widget is set up. */
internal fun Preferences.highlightsConfig(): HighlightsWidgetConfig? = this[HighlightsProjectKey]?.let { projectId ->
    HighlightsWidgetConfig(
        projectId = projectId,
        range = this[HighlightsRangeKey]?.let { name -> AnalyticsRange.entries.firstOrNull { it.name == name } }
            ?: HighlightsWidgetConfig.DefaultRange,
        metrics = this[HighlightsMetricsKey]?.split(',')
            ?.mapNotNull { name -> HighlightMetric.entries.firstOrNull { it.name == name } }
            ?.distinct()
            ?.takeIf { it.isNotEmpty() }
            ?: HighlightsWidgetConfig.DefaultMetrics,
        projectName = this[HighlightsProjectNameKey],
    )
}

/**
 * A project's highlights, picked per widget from its web stats, active users, revenue and app health: a grid of tiles,
 * as many as fit in as few columns as show them, each a metric's value and, when there's room, its change over the
 * widget's range. Loads when its session starts (every 30 minutes, see `highlights_widget_info.xml`), and through
 * [HighlightsResults] when the account changes, the refresh button is tapped or its settings change; a tile whose
 * request failed says so, and the widget shows the error only when they all did.
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
            HighlightsGrid(state.content.tiles, title, range, config.projectName, load, refresh)
        }

        else -> WidgetNotice(state, title, range, load, refresh)
    }
}

/**
 * The header, then the tiles in a grid laid out by [highlightsLayout], and the footer when there is room left for it;
 * the header says how many tiles didn't fit. One cell tall, the project and range on one line, if they fit, over a row
 * of tiles without cards.
 */
@Composable
private fun HighlightsGrid(
    tiles: List<HighlightTile>,
    title: String,
    range: String,
    project: String?,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val fontScale = context.resources.configuration.fontScale
    val compact = size.height < CompactHeight
    val verticalPadding = if (compact) CompactVerticalPadding else HighlightsPadding
    val tilePadding = if (compact) 0.dp else TileVerticalPadding
    val inner = size.height - verticalPadding * 2
    val header = headerHeight(compact, inner, minTileHeight(fontScale, tilePadding))
    val layout = highlightsLayout(
        width = size.width - HighlightsPadding * 2,
        height = inner - (header ?: 0.dp),
        count = tiles.size,
        values = tiles.map { tile -> tile.value.orEmpty() },
        fontScale = fontScale,
        // The footer's caption and the gap above it.
        footerHeight = FooterHeight.scaled() + TileGap,
        tilePadding = tilePadding,
    )
    val hidden = tiles.size - minOf(tiles.size, layout.shown)
    val caption = if (hidden > 0) context.getString(R.string.widget_highlights_range_more, range, hidden) else range
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .padding(horizontal = HighlightsPadding, vertical = verticalPadding),
    ) {
        when {
            header == null -> Unit
            compact -> WidgetHeader(withProject(project, caption), null, refresh, load.isReloading)
            else -> WidgetHeader(title, caption, refresh, load.isReloading)
        }
        if (header != null) Spacer(modifier = GlanceModifier.height(TileGap))
        // A column of their own: Glance draws at most ten children of one, which the rows and the gaps between them
        // would pass beside the header and footer. The gaps are the rows' padding for the same reason.
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            tiles.take(layout.shown).chunked(layout.columns).forEachIndexed { index, row ->
                TileRow(row, layout, carded = !compact, top = if (index > 0) TileGap else 0.dp)
            }
        }
        if (layout.showsFooter && !compact) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            WidgetFooter(load.loadedAt)
        }
    }
}

/** What a header of a single line takes: its refresh button, taller than the line. */
private val CompactHeaderHeight = 32.dp

/**
 * What the header takes with the gap under it, out of [inner], the widget's height inside its padding; null when it
 * goes. One cell tall, it goes when it would leave a tile less than [minTile]: the numbers matter more.
 */
@Composable
private fun headerHeight(compact: Boolean, inner: Dp, minTile: Dp): Dp? = when {
    !compact -> maxOf(HeaderHeight.scaled(), CompactHeaderHeight) + TileGap
    inner - CompactHeaderHeight - TileGap >= minTile -> CompactHeaderHeight + TileGap
    else -> null
}

/**
 * [tiles] side by side, each a column's width, so a short last row lines up with those above; [top] is the gap above
 * the row.
 */
@Composable
private fun TileRow(tiles: List<HighlightTile>, layout: HighlightsLayout, carded: Boolean, top: Dp) {
    Row(modifier = GlanceModifier.fillMaxWidth().height(layout.tileHeight + top).padding(top = top)) {
        repeat(layout.columns) { index ->
            if (index > 0) Spacer(modifier = GlanceModifier.width(TileGap))
            val modifier = GlanceModifier.defaultWeight().fillMaxHeight()
            tiles.getOrNull(index)?.let { HighlightTileView(it, layout, carded, modifier) }
                ?: Spacer(modifier = modifier)
        }
    }
}

/**
 * A metric's name over its value, then its change when there is room; without room, an arrow by the name says which
 * way it went. A tile without a value says why in its place.
 */
@Composable
private fun HighlightTileView(
    tile: HighlightTile,
    layout: HighlightsLayout,
    carded: Boolean,
    modifier: GlanceModifier,
) {
    val card = if (carded) {
        modifier.background(WidgetColors.surfaceContainer).cornerRadius(TileCornerRadius)
            .padding(horizontal = TileHorizontalPadding, vertical = TileVerticalPadding)
    } else {
        modifier
    }
    Column(modifier = card, verticalAlignment = Alignment.CenterVertically) {
        val change = tile.change
        val label = string(tile.metric.tileLabel)
        val fontScale = LocalContext.current.resources.configuration.fontScale
        // The arrow only when the name still fits beside it: the name says what the number is.
        val arrow = change?.takeIf {
            val labelWidth = (emWidth(label) * RegularEm * TileCaptionSp * fontScale).dp
            !layout.showsChange && labelWidth + ArrowWidth <= layout.textWidth
        }
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // On a card the arrow goes to its edge; without one, right after the name, or it reads as the next tile's.
            WidgetCaption(label, modifier = if (carded) GlanceModifier.defaultWeight() else GlanceModifier)
            arrow?.let { ChangeArrow(it) }
        }
        val value = tile.value
        val missing = tile.missing
        when {
            value != null -> WidgetValue(value, fontSize = layout.valueSp.sp)
            missing != null -> MissingNote(string(missing.label), layout)
            else -> WidgetValue("–", fontSize = layout.valueSp.sp)
        }
        if (change != null && layout.showsChange) WidgetChangeText(change, fontSize = TileCaptionSp.sp)
    }
}

private val TileCornerRadius: Dp = 12.dp

/** What [ChangeArrow] takes beside a tile's name, with its padding. */
private val ArrowWidth = 12.dp

/** A tile's name is regular, not bold: about this much of the width [emWidth] gives, measured on the widgets. */
private const val RegularEm = 0.75f

/**
 * Why a tile has no value, in its place: on two lines when the tile has room for them and its longest word fits a
 * line, since Android would break that word apart; otherwise on one, cut short.
 */
@Composable
private fun MissingNote(text: String, layout: HighlightsLayout) {
    val fontScale = LocalContext.current.resources.configuration.fontScale
    val longestWord = text.split(' ').maxOfOrNull(::emWidth) ?: 0f
    val wordsFit = (longestWord * TileNoteSp * fontScale).dp <= layout.textWidth
    Text(
        text = text,
        maxLines = if (layout.showsChange && wordsFit) 2 else 1,
        style = TextStyle(color = WidgetColors.onSurfaceVariant, fontSize = TileNoteSp.sp),
    )
}

/** Just the arrow of [change] ("↑"), coloured as its text would be. */
@Composable
private fun ChangeArrow(change: TrendChange) = Text(
    text = change.text.substringBefore(' '),
    maxLines = 1,
    modifier = GlanceModifier.padding(start = 4.dp),
    style = TextStyle(
        color = when (change.tone) {
            ChangeTone.Good -> WidgetColors.good
            ChangeTone.Bad -> WidgetColors.bad
            ChangeTone.Neutral -> WidgetColors.onSurfaceVariant
        },
        fontSize = TileCaptionSp.sp,
        fontWeight = FontWeight.Bold,
    ),
)

private val HighlightMissing.label: Int
    get() = when (this) {
        HighlightMissing.NotSetUp -> R.string.widget_highlight_not_set_up
        HighlightMissing.NoSubscriptions -> R.string.widget_highlight_no_subscriptions
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
        HighlightMetric.Stickiness -> R.string.widget_tile_stickiness
        HighlightMetric.Mrr -> R.string.widget_tile_mrr
        HighlightMetric.Revenue -> R.string.widget_tile_revenue
        HighlightMetric.Subscribers -> R.string.widget_tile_subscribers
        HighlightMetric.PayingCustomers -> R.string.widget_tile_paying_customers
        HighlightMetric.LatestAppVersion -> R.string.widget_tile_version
        HighlightMetric.Exceptions -> R.string.widget_tile_exceptions
        HighlightMetric.UsersWithErrors -> R.string.widget_tile_users_with_errors
    }
