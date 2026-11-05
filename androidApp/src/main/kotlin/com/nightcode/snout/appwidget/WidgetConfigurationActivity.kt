package com.nightcode.snout.appwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * The settings a widget opens when it is added and when it is reconfigured. Leaving without saving cancels adding the
 * widget, as Android expects of a configuration activity, and leaves a placed one as it was.
 */
abstract class WidgetConfigurationActivity : ComponentActivity() {

    /** Loads the widget again with what was just saved. */
    protected abstract suspend fun reload(glanceId: GlanceId)

    /** Reads the widget's Glance state. */
    protected abstract val widget: GlanceAppWidget

    /** The receiver of the widgets these settings are for. */
    protected abstract val receiver: Class<out GlanceAppWidgetReceiver>

    protected var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        private set

    private lateinit var result: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, result)
        // Exported for the launcher, so any app can open it: only a placed widget of this kind has settings here.
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.provider
        if (provider?.packageName != packageName || provider.className != receiver.name) return finish()
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigurationActivity).getGlanceIdBy(appWidgetId)
            showSettings(glanceId)
        }
    }

    /** Shows the settings of the widget [glanceId]; [save] stores what the person picked. */
    protected abstract suspend fun showSettings(glanceId: GlanceId)

    /** The widget's settings as stored, before this activity changes them. */
    protected suspend fun currentState(glanceId: GlanceId): Preferences = widget.getAppWidgetState(this, glanceId)

    /** Stores [edit] in the widget's state, loads it with what it now shows and closes. */
    protected fun save(glanceId: GlanceId, edit: (MutablePreferences) -> Unit) {
        lifecycleScope.launch {
            updateAppWidgetState(this@WidgetConfigurationActivity, glanceId, edit)
            reload(glanceId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
