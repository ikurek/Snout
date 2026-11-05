package com.nightcode.snout.appwidget

import androidx.activity.compose.setContent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import com.nightcode.snout.features.widgets.InsightWidgetPicker

/** Picks the project and insight an [InsightWidget] shows, when the widget is added and when it is reconfigured. */
class InsightWidgetConfigurationActivity : WidgetConfigurationActivity() {

    override val widget: GlanceAppWidget = InsightWidget()

    override val receiver = InsightWidgetReceiver::class.java

    override suspend fun reload(glanceId: GlanceId) = InsightResults.reload(this, glanceId)

    override suspend fun showSettings(glanceId: GlanceId) {
        // A widget being reconfigured starts on its own project.
        val projectId = currentState(glanceId)[InsightProjectKey]
        setContent {
            InsightWidgetPicker(
                projectId = projectId,
                onInsightPicked = { project, insightId, name ->
                    save(glanceId) {
                        it[InsightProjectKey] = project.id
                        it[InsightProjectNameKey] = project.name
                        it[InsightIdKey] = insightId
                        it[InsightNameKey] = name
                    }
                },
                onClose = ::finish,
            )
        }
    }
}
