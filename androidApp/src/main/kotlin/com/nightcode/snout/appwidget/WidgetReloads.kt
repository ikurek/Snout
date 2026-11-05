package com.nightcode.snout.appwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.minutes

/** What a widget draws: what loading [config] gave, when that finished, and whether a reload is running. */
internal data class WidgetLoad<C, T>(val config: C, val result: T, val loadedAt: Long, val isReloading: Boolean = false)

/**
 * Outlives any one session or broadcast, so a load finishes however long PostHog takes. A redraw that fails (the
 * Glance state can't be written) leaves the widget as it was, rather than taking the app down with it.
 */
internal val WidgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })

/** How long a load stands in for a new one, so the redraw after a reload doesn't load again. */
private val Fresh = 1.minutes

/**
 * The last load of each placed [widget], held in memory only, like a screen's state in its view model: after the
 * process ends, a widget loads again. [config] reads a widget's settings from its Glance state, [load] loads for them.
 *
 * Reloads run in [WidgetScope], not in the widget's session: Glance cancels a session 45 seconds after it first draws
 * (a tap adds 5), and an insight can take PostHog longer, so a reload cut short with the session would leave its
 * spinner drawn. Glance has no way to close a session either, to load again where it doesn't time anything: before
 * the session's first drawing.
 */
internal class WidgetResults<C, T>(
    private val widget: GlanceAppWidget,
    private val config: (Preferences) -> C,
    private val load: suspend (C) -> T,
) {
    private val loads = MutableStateFlow<Map<GlanceId, WidgetLoad<C, T>>>(emptyMap())
    private val reloads = ConcurrentHashMap<GlanceId, Job>()

    /** The latest load started for each widget; an earlier one that finishes after it is dropped. */
    private val latest = ConcurrentHashMap<GlanceId, Long>()
    private val started = AtomicLong()

    /**
     * Draws [content] with what widget [id] loaded, and again each time [reload] finishes. It loads first, after a
     * reload running, unless one finished within the last minute; meanwhile the launcher keeps the previous drawing (or
     * the loading layout of a widget just placed) instead of an empty frame.
     */
    suspend fun provide(context: Context, id: GlanceId, content: @Composable (WidgetLoad<C, T>) -> Unit): Nothing {
        reloads[id]?.join()
        val config = config(widget.getAppWidgetState(context, id))
        val first = loads.value[id]
            ?.takeIf { it.config == config && System.currentTimeMillis() - it.loadedAt < Fresh.inWholeMilliseconds }
            ?: keep(id, config)
        widget.provideContent {
            val shown by remember { loads.mapNotNull { it[id] } }.collectAsState(first)
            content(shown)
        }
    }

    /**
     * Loads widget [id] again, with the settings in its Glance state, and redraws it with the result; meanwhile it
     * shows what it showed with a spinner. A reload still running is dropped for this one.
     */
    suspend fun reload(context: Context, id: GlanceId) {
        val config = config(widget.getAppWidgetState(context, id))
        loads.update { all -> all[id]?.let { all + (id to it.copy(isReloading = true)) } ?: all }
        val job = WidgetScope.launch(start = CoroutineStart.LAZY) {
            keep(id, config)
            widget.update(context, id)
        }
        reloads.put(id, job)?.cancel()
        job.start()
        widget.update(context, id)
    }

    /** Loads every placed widget of this kind again. */
    suspend fun reloadAll(context: Context) {
        GlanceAppWidgetManager(context).getGlanceIds(widget.javaClass).forEach { reload(context, it) }
    }

    /** Drops what widget [id] showed, once it is removed from the home screen. */
    fun forget(id: GlanceId) {
        reloads.remove(id)?.cancel()
        latest.remove(id)
        loads.update { it - id }
    }

    /**
     * Loads [config] for widget [id] and keeps the result, unless a load started after this one (a session's first
     * load still running when the person signs out, say) did; then that load's result is the one returned.
     */
    private suspend fun keep(id: GlanceId, config: C): WidgetLoad<C, T> {
        val generation = started.incrementAndGet().also { latest[id] = it }
        val done = WidgetLoad(config, load(config), System.currentTimeMillis())
        return loads.updateAndGet { all -> if (latest[id] == generation) all + (id to done) else all }[id] ?: done
    }
}
