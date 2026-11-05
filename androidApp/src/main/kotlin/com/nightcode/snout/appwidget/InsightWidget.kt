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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nightcode.snout.R
import com.nightcode.snout.widgets.InsightWidgetBody
import com.nightcode.snout.widgets.InsightWidgetContent
import com.nightcode.snout.widgets.InsightWidgetLoader
import com.nightcode.snout.widgets.RankedValue
import com.nightcode.snout.widgets.WidgetState
import org.koin.mp.KoinPlatform

/**
 * The insight a widget shows, its project and its name, picked in [InsightWidgetConfigurationActivity]; configuration
 * only. The name titles the widget while it loads or fails; a load shows the current one. The project's name, as it
 * was when picked, says which project the widget shows.
 */
internal val InsightProjectKey = longPreferencesKey("project_id")
internal val InsightProjectNameKey = stringPreferencesKey("project_name")
internal val InsightIdKey = longPreferencesKey("insight_id")
internal val InsightNameKey = stringPreferencesKey("insight_name")

private val RankingRowHeight = 22.dp

/** The value and change under a headline's header, at the default font scale. */
private val HeadlineHeight = 58.dp

/** Which insight a widget shows; null until one is picked. */
internal data class InsightPick(val projectId: Long?, val insightId: Long, val name: String?, val project: String?)

private fun Preferences.insightPick(): InsightPick? = this[InsightIdKey]?.let {
    InsightPick(this[InsightProjectKey], it, this[InsightNameKey], this[InsightProjectNameKey])
}

/** No insight picked yet, or what loading the picked one gave. */
internal sealed interface InsightLoad {
    data object NotChosen : InsightLoad

    data class Done(val state: WidgetState<InsightWidgetContent>) : InsightLoad
}

/**
 * One saved insight: its latest number with its line, its top breakdowns, or its funnel's conversion. Loads when its
 * session starts, and through [InsightResults] when the account changes, the refresh button is tapped or another
 * insight is picked; a failed load shows the error.
 */
internal class InsightWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override val previewSizeMode = PreviewSizes

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val configure = configureAction(
            context,
            InsightWidgetConfigurationActivity::class.java,
            GlanceAppWidgetManager(context).getAppWidgetId(id),
        )
        InsightResults.provide(context, id) { load -> InsightWidgetUi(load, configure) }
    }

    /** The widget picker's preview: this widget with sample numbers (see [publishWidgetPreviews]). */
    override suspend fun providePreview(context: Context, widgetCategory: Int) = provideContent {
        InsightWidgetUi(insightPreview(context), openInApp(context, link = null))
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) = InsightResults.forget(glanceId)
}

/** What each insight widget loaded. */
internal val InsightResults = WidgetResults(InsightWidget(), { it.insightPick() }) { pick: InsightPick? ->
    when (val projectId = pick?.projectId) {
        // Picked before widgets had a project of their own: it can't tell which, so it asks again.
        null -> InsightLoad.NotChosen

        else -> InsightLoad.Done(KoinPlatform.getKoin().get<InsightWidgetLoader>().load(projectId, pick.insightId))
    }
}

class InsightWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = InsightWidget()
}

/** Loads the widget again, for its refresh button and a tap on an error. */
class RefreshInsightWidget : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) =
        InsightResults.reload(context, glanceId)
}

@Composable
internal fun InsightWidgetUi(load: WidgetLoad<InsightPick?, InsightLoad>, configure: Action) {
    val title = load.config?.name ?: string(R.string.widget_insight_name)
    val project = load.config?.project
    val refresh = actionRunCallback<RefreshInsightWidget>()
    when (val result = load.result) {
        InsightLoad.NotChosen -> WidgetMessageFrame(
            title = title,
            caption = null,
            message = string(R.string.widget_insight_not_chosen),
            hint = null,
            onClick = configure,
            retry = refresh,
            load = load,
        )

        is InsightLoad.Done -> when (val state = result.state) {
            is WidgetState.Loaded -> LoadedInsight(state.content, project, load, refresh)
            else -> WidgetNotice(state, title, caption = project, load = load, retry = refresh)
        }
    }
}

@Composable
private fun LoadedInsight(content: InsightWidgetContent, project: String?, load: WidgetLoad<*, *>, refresh: Action) {
    val open = openInApp(LocalContext.current, content.link)
    when (val body = content.body) {
        is InsightWidgetBody.Headline -> WidgetFrame(open, padded = false) {
            Headline(content.name, project, body, load, refresh)
        }

        is InsightWidgetBody.Ranking -> WidgetFrame(open) { Ranking(content.name, project, body.rows, load, refresh) }

        InsightWidgetBody.Unsupported -> WidgetMessageFrame(
            title = content.name,
            caption = project,
            message = string(R.string.widget_insight_unsupported),
            hint = null,
            onClick = open,
            retry = refresh,
            load = load,
        )
    }
}

/**
 * The insight's number, its change and what it counts, over its line filling the rest of the widget edge to edge
 * when there is room for one; framed without it otherwise.
 */
@Composable
private fun ColumnScope.Headline(
    name: String,
    project: String?,
    body: InsightWidgetBody.Headline,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val size = LocalSize.current
    if (size.height < CompactHeight) {
        Column(modifier = GlanceModifier.fillMaxSize().framePadding()) {
            CompactHeader(name, project, load, refresh)
            Spacer(modifier = GlanceModifier.defaultWeight())
            Row(verticalAlignment = Alignment.Bottom) {
                WidgetValue(body.value, fontSize = 26.sp)
                body.change?.let {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Column(modifier = GlanceModifier.padding(bottom = 5.dp)) { WidgetChangeText(it) }
                }
            }
        }
        return
    }
    val chartHeight = size.height - WidgetPadding - (HeaderHeight + HeadlineHeight).scaled()
    if (body.series.size > 1 && chartHeight >= MinChartHeight.scaled()) {
        WidgetChartTop { HeadlineNumber(name, project, body, load, refresh) }
        WidgetChart(body.series, size.width, chartHeight, load.loadedAt)
        return
    }
    Column(modifier = GlanceModifier.fillMaxSize().framePadding()) {
        HeadlineNumber(name, project, body, load, refresh)
        Spacer(modifier = GlanceModifier.defaultWeight())
        WidgetFooter(load.loadedAt)
    }
}

@Composable
private fun HeadlineNumber(
    name: String,
    project: String?,
    body: InsightWidgetBody.Headline,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    WidgetHeader(name, project, refresh, load.isReloading)
    WidgetValue(body.value)
    // What the number counts (the series' event), after its change.
    if (body.change != null || body.label != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            body.change?.let { WidgetChangeText(it) }
            body.label?.let { label ->
                if (body.change != null) Spacer(modifier = GlanceModifier.width(6.dp))
                WidgetCaption(label)
            }
        }
    }
}

@Composable
private fun ColumnScope.Ranking(
    name: String,
    project: String?,
    rows: List<RankedValue>,
    load: WidgetLoad<*, *>,
    refresh: Action,
) {
    val size = LocalSize.current
    val compact = size.height < CompactHeight
    if (compact) {
        CompactHeader(name, project, load, refresh)
    } else {
        WidgetHeader(name, project, refresh, load.isReloading)
    }
    val footer = if (compact) 0.dp else FooterHeight
    val room = size.height - WidgetPadding * 2 - (HeaderHeight + footer).scaled()
    val count = (room / RankingRowHeight.scaled()).toInt().coerceIn(1, rows.size)
    Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp)) {
        rows.take(count).forEach { row -> RankingRow(row) }
    }
    if (!compact) {
        Spacer(modifier = GlanceModifier.defaultWeight())
        WidgetFooter(load.loadedAt)
    }
}

@Composable
private fun RankingRow(row: RankedValue) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(color = WidgetColors.onBackground, fontSize = 13.sp),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = row.value,
            maxLines = 1,
            style = TextStyle(color = WidgetColors.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold),
        )
    }
}
