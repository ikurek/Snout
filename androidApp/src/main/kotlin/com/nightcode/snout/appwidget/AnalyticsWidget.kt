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
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.nightcode.snout.R
import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.widgets.AnalyticsWidgetConfig
import com.nightcode.snout.widgets.AnalyticsWidgetContent
import com.nightcode.snout.widgets.AnalyticsWidgetLoader
import com.nightcode.snout.widgets.WidgetStat
import com.nightcode.snout.widgets.WidgetState
import org.koin.mp.KoinPlatform

/**
 * The project, stat and range a widget shows, set in [AnalyticsWidgetConfigurationActivity]; configuration only. The
 * project's name, as it was when picked, says which project the widget shows.
 */
internal val AnalyticsProjectKey = longPreferencesKey("project_id")
internal val AnalyticsProjectNameKey = stringPreferencesKey("project_name")
internal val AnalyticsMetricKey = stringPreferencesKey("metric")
internal val AnalyticsRangeKey = stringPreferencesKey("range")

/** From this width a wide widget lists a third stat beside (or under) its lead; two before. */
private val ThreeStatsWidth = 420.dp
private const val OtherStats = 2

/** Without a chart, a wide widget lists the other stats from this height, and the footer from [WideFooterHeight]. */
private val WideStatsHeight = 160.dp
private val WideFooterHeight = 190.dp

/** What a hero value and its change take, at the default font scale. */
private val HeroHeight = 58.dp

/** A small stat's name, value and change. */
private val SmallStatHeight = 50.dp

/** Null until the widget is set up: one placed before widgets had a project of their own has none. */
internal fun Preferences.analyticsConfig(): AnalyticsWidgetConfig? = this[AnalyticsProjectKey]?.let { projectId ->
    AnalyticsWidgetConfig(
        projectId = projectId,
        metric = this[AnalyticsMetricKey]?.let { name -> OverviewMetricKey.entries.firstOrNull { it.name == name } }
            ?: AnalyticsWidgetConfig.DefaultMetric,
        range = this[AnalyticsRangeKey]?.let { name -> AnalyticsRange.entries.firstOrNull { it.name == name } }
            ?: AnalyticsWidgetConfig.DefaultRange,
        projectName = this[AnalyticsProjectNameKey],
    )
}

/**
 * One analytics stat of a project over a range, all three picked per widget (web analytics count a mobile app's
 * screens as page views, as the Analytics tab does): the stat and its change when one cell tall, with its line below
 * when taller, and next to it when wide, with the other stats under it. The header names the project. Loads when its
 * session starts (every 30 minutes, see `analytics_widget_info.xml`), and through [AnalyticsResults] when the account
 * changes, the refresh button is tapped or its settings change; a failed load shows the error.
 */
internal class AnalyticsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override val previewSizeMode = PreviewSizes

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val configure = configureAction(
            context,
            AnalyticsWidgetConfigurationActivity::class.java,
            GlanceAppWidgetManager(context).getAppWidgetId(id),
        )
        AnalyticsResults.provide(context, id) { load -> AnalyticsWidgetUi(load, configure) }
    }

    /** The widget picker's preview: this widget with sample numbers (see [publishWidgetPreviews]). */
    override suspend fun providePreview(context: Context, widgetCategory: Int) = provideContent {
        AnalyticsWidgetUi(analyticsPreview(context), openInApp(context, link = null))
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) = AnalyticsResults.forget(glanceId)
}

/** What each analytics widget loaded; nulls until the widget is set up. */
internal val AnalyticsResults = WidgetResults(AnalyticsWidget(), Preferences::analyticsConfig) { config ->
    config?.let { KoinPlatform.getKoin().get<AnalyticsWidgetLoader>().load(it) }
}

class AnalyticsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AnalyticsWidget()
}

/** Loads the widget again, for its refresh button and a tap on an error. */
class RefreshAnalyticsWidget : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) =
        AnalyticsResults.reload(context, glanceId)
}

/** [load] holds nulls until the widget is set up. */
@Composable
internal fun AnalyticsWidgetUi(
    load: WidgetLoad<AnalyticsWidgetConfig?, WidgetState<AnalyticsWidgetContent>?>,
    configure: Action,
) {
    val refresh = actionRunCallback<RefreshAnalyticsWidget>()
    val config = load.config
    val state = load.result
    if (config == null || state == null) {
        return WidgetMessageFrame(
            title = string(R.string.widget_analytics_name),
            caption = null,
            message = string(R.string.widget_analytics_not_set_up),
            hint = null,
            onClick = configure,
            retry = refresh,
            load = load,
        )
    }
    val title = string(config.metric.label)
    val range = string(config.range.label)
    when (state) {
        is WidgetState.Loaded -> WidgetFrame(openInApp(LocalContext.current, state.content.link), padded = false) {
            AnalyticsContent(state.content, title, range, config.projectName, load, refresh)
        }

        else -> WidgetNotice(state, title, withProject(config.projectName, range), load, refresh)
    }
}

/**
 * The stat over its line when there is room for one: the header and the number (with the other stats beside it when
 * wide), and the line filling the rest of the widget edge to edge. Otherwise the stat and the other stats, framed.
 */
@Composable
private fun ColumnScope.AnalyticsContent(
    content: AnalyticsWidgetContent,
    title: String,
    range: String,
    project: String?,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val size = LocalSize.current
    val lead = content.stats.firstOrNull() ?: WidgetStat(OverviewMetricKey.Other, "–", null)
    val caption = withProject(project, range)
    val chartHeight = size.height - WidgetPadding - (HeaderHeight + HeroHeight).scaled()
    if (size.height >= CompactHeight && content.series.size > 1 && chartHeight >= MinChartHeight.scaled()) {
        return ChartedAnalytics(content, lead, title, caption, chartHeight, load, refresh)
    }
    Column(modifier = GlanceModifier.fillMaxSize().framePadding()) {
        when {
            size.height < CompactHeight -> {
                CompactHeader(
                    title = LocalContext.current.getString(R.string.widget_title_with_range, title, range),
                    project = project,
                    load = load,
                    refresh = refresh,
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                HeroRow(lead)
            }

            size.width >= WideWidth -> WideAnalytics(content, lead, title, caption, load, refresh)

            else -> {
                WidgetHeader(title, caption, refresh, load.isReloading)
                WidgetValue(lead.value)
                lead.change?.let { WidgetChangeText(it) }
                val room = size.height - WidgetPadding * 2 - (HeaderHeight + HeroHeight + FooterHeight).scaled()
                Spacer(modifier = GlanceModifier.defaultWeight())
                // No line for this stat (or none from PostHog): the next two stats take its place.
                if (room >= SmallStatHeight.scaled()) {
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        content.stats.drop(1).take(2).forEach { SmallStat(it, GlanceModifier.defaultWeight()) }
                    }
                }
                WidgetFooter(load.loadedAt)
            }
        }
    }
}

/** The header and the stat (with the other stats beside it when wide) over its line, edge to edge. */
@Composable
private fun ColumnScope.ChartedAnalytics(
    content: AnalyticsWidgetContent,
    lead: WidgetStat,
    title: String,
    caption: String,
    chartHeight: Dp,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val size = LocalSize.current
    WidgetChartTop {
        WidgetHeader(title, caption, refresh, load.isReloading)
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = GlanceModifier.padding(end = 16.dp)) {
                WidgetValue(lead.value)
                lead.change?.let { WidgetChangeText(it) }
            }
            if (size.width >= WideWidth) {
                otherStats(content, size.width).forEach { SmallStat(it, GlanceModifier.defaultWeight()) }
            }
        }
    }
    WidgetChart(content.series, size.width, chartHeight, load.loadedAt)
}

/** The stats after the lead that a wide widget of [width] lists. */
private fun otherStats(content: AnalyticsWidgetContent, width: Dp): List<WidgetStat> =
    content.stats.drop(1).take(if (width >= ThreeStatsWidth) OtherStats + 1 else OtherStats)

/** A wide widget without a line: the stat, the other stats under it and the footer, as the height allows. */
@Composable
private fun ColumnScope.WideAnalytics(
    content: AnalyticsWidgetContent,
    lead: WidgetStat,
    title: String,
    caption: String,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val size = LocalSize.current
    WidgetHeader(title, caption, refresh, load.isReloading)
    WidgetValue(lead.value)
    lead.change?.let { WidgetChangeText(it) }
    if (size.height >= WideStatsHeight.scaled()) {
        val others = otherStats(content, size.width)
        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 10.dp)) {
            others.forEach { stat -> SmallStat(stat, GlanceModifier.defaultWeight()) }
        }
    }
    Spacer(modifier = GlanceModifier.defaultWeight())
    if (size.height >= WideFooterHeight.scaled()) {
        WidgetFooter(load.loadedAt)
    }
}

/** The lead stat's value with its change beside it, for a widget one cell tall. */
@Composable
private fun HeroRow(stat: WidgetStat) {
    Row(verticalAlignment = Alignment.Bottom) {
        WidgetValue(stat.value, fontSize = 26.sp)
        stat.change?.let {
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(modifier = GlanceModifier.padding(bottom = 5.dp)) { WidgetChangeText(it) }
        }
    }
}

@Composable
private fun SmallStat(stat: WidgetStat, modifier: GlanceModifier) {
    Column(modifier = modifier.padding(end = 8.dp)) {
        WidgetCaption(string(stat.key.label))
        WidgetValue(stat.value, fontSize = 16.sp)
        stat.change?.let { WidgetChangeText(it, fontSize = 11.sp) }
    }
}

/** [this] at the person's font scale: text grows with it, so what holds text needs more room. */
@Composable
internal fun Dp.scaled(): Dp = this * LocalContext.current.resources.configuration.fontScale

internal val AnalyticsRange.label: Int
    get() = when (this) {
        AnalyticsRange.Today -> R.string.widget_range_today
        AnalyticsRange.Last7Days -> R.string.widget_range_7d
        AnalyticsRange.Last30Days -> R.string.widget_range_30d
        AnalyticsRange.Last90Days -> R.string.widget_range_90d
    }

internal val OverviewMetricKey.label: Int
    get() = when (this) {
        OverviewMetricKey.Visitors -> R.string.widget_metric_visitors
        OverviewMetricKey.Views -> R.string.widget_metric_views
        OverviewMetricKey.Sessions -> R.string.widget_metric_sessions
        OverviewMetricKey.SessionDuration -> R.string.widget_metric_session_duration
        OverviewMetricKey.BounceRate -> R.string.widget_metric_bounce_rate
        OverviewMetricKey.WeeklyActiveUsers -> R.string.widget_metric_wau
        OverviewMetricKey.MonthlyActiveUsers -> R.string.widget_metric_mau
        OverviewMetricKey.Other -> R.string.widget_metric_other
    }
