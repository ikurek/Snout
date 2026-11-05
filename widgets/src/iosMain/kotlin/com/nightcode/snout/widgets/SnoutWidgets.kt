package com.nightcode.snout.widgets

import com.nightcode.snout.shared.domain.model.analytics.AnalyticsRange
import com.nightcode.snout.shared.domain.model.analytics.HighlightMetric
import com.nightcode.snout.shared.domain.model.analytics.OverviewMetricKey
import com.nightcode.snout.shared.widgets.WidgetSession
import com.nightcode.snout.shared.widgets.WidgetSharedModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import org.koin.plugin.module.dsl.module

/**
 * What the iOS widget extension calls from Swift. The extension is a process of its own: [start] builds its graph,
 * which reads the connection the app shares with it (the App Group's preferences and the shared Keychain item, see
 * `SharedWithWidgets`) and never writes it.
 */
object SnoutWidgets {

    /** Starts the extension's graph; WidgetKit may ask for several timelines per process, so later calls do nothing. */
    fun start() {
        if (KoinPlatform.getKoinOrNull() != null) return
        startKoin {
            module<WidgetSharedModule>()
            module<WidgetsModule>()
        }
    }

    suspend fun analytics(
        projectId: Long,
        metric: OverviewMetricKey,
        range: AnalyticsRange,
    ): WidgetState<AnalyticsWidgetContent> =
        fresh<AnalyticsWidgetLoader>().load(AnalyticsWidgetConfig(projectId, metric, range))

    suspend fun highlights(
        projectId: Long,
        range: AnalyticsRange,
        metrics: List<HighlightMetric>,
    ): WidgetState<HighlightsWidgetContent> =
        fresh<HighlightsWidgetLoader>().load(HighlightsWidgetConfig(projectId, range, metrics))

    suspend fun insight(projectId: Long, insightId: Long): WidgetState<InsightWidgetContent> =
        fresh<InsightWidgetLoader>().load(projectId, insightId)

    /** For a widget's settings: the projects to pick from. */
    suspend fun projects(): WidgetState<List<ProjectChoice>> = fresh<WidgetProjects>().projects()

    /** For the insight widget's settings: [projectId]'s insights to pick from. */
    suspend fun insightChoices(projectId: Long, query: String): WidgetState<List<InsightChoice>> =
        fresh<InsightWidgetLoader>().choices(projectId, query)

    /** [T] from the graph, once the extension has caught up with the app (see [WidgetSession]). */
    private suspend inline fun <reified T : Any> fresh(): T {
        KoinPlatform.getKoin().get<WidgetSession>().refresh()
        return KoinPlatform.getKoin().get<T>()
    }
}
