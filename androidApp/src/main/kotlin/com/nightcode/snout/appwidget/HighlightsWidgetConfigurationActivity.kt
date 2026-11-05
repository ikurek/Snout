package com.nightcode.snout.appwidget

import androidx.activity.compose.setContent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import com.nightcode.snout.features.widgets.HighlightsWidgetSettings

/**
 * Picks the project, range and metrics a [HighlightsWidget] shows, when the widget is added and when it is
 * reconfigured.
 */
class HighlightsWidgetConfigurationActivity : WidgetConfigurationActivity() {

    override val widget: GlanceAppWidget = HighlightsWidget()

    override val receiver = HighlightsWidgetReceiver::class.java

    override suspend fun reload(glanceId: GlanceId) = HighlightsResults.reload(this, glanceId)

    override suspend fun showSettings(glanceId: GlanceId) {
        val state = currentState(glanceId)
        setContent {
            HighlightsWidgetSettings(
                // A widget being added has no settings yet; one being reconfigured has.
                initial = state.highlightsConfig(),
                onSave = { config ->
                    save(glanceId) {
                        it[HighlightsProjectKey] = config.projectId
                        config.projectName?.let { name -> it[HighlightsProjectNameKey] = name }
                        it[HighlightsRangeKey] = config.range.name
                        it[HighlightsMetricsKey] = config.metrics.joinToString(",") { metric -> metric.name }
                    }
                },
                onClose = ::finish,
            )
        }
    }
}
