package com.nightcode.snout.appwidget

import android.content.Context
import com.nightcode.snout.observeWidgetChanges
import kotlinx.coroutines.launch

/**
 * What the widgets need from the app's start: their previews in the widget picker, and a reload each time the
 * person signs in or out in the app. The widgets can't notice that on their own before their next refresh, up to 30
 * minutes later: until then they would show the account that signed out, or still ask to sign in.
 */
internal fun startWidgets(context: Context) {
    WidgetScope.launch { publishWidgetPreviews(context) }
    observeWidgetChanges {
        WidgetScope.launch {
            AnalyticsResults.reloadAll(context)
            InsightResults.reloadAll(context)
            HighlightsResults.reloadAll(context)
        }
    }
}
