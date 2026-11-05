package com.nightcode.snout.appwidget

import androidx.activity.compose.setContent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import com.nightcode.snout.features.widgets.AnalyticsWidgetSettings

/**
 * Picks the project, stat and range an [AnalyticsWidget] shows, when the widget is added and when it is reconfigured.
 */
class AnalyticsWidgetConfigurationActivity : WidgetConfigurationActivity() {

    override val widget: GlanceAppWidget = AnalyticsWidget()

    override val receiver = AnalyticsWidgetReceiver::class.java

    override suspend fun reload(glanceId: GlanceId) = AnalyticsResults.reload(this, glanceId)

    override suspend fun showSettings(glanceId: GlanceId) {
        val state = currentState(glanceId)
        setContent {
            AnalyticsWidgetSettings(
                // A widget being added has no settings yet; one being reconfigured has.
                initial = state.analyticsConfig(),
                onSave = { config ->
                    save(glanceId) {
                        it[AnalyticsProjectKey] = config.projectId
                        config.projectName?.let { name -> it[AnalyticsProjectNameKey] = name }
                        it[AnalyticsMetricKey] = config.metric.name
                        it[AnalyticsRangeKey] = config.range.name
                    }
                },
                onClose = ::finish,
            )
        }
    }
}
