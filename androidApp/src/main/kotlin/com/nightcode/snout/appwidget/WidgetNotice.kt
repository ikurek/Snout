package com.nightcode.snout.appwidget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import com.nightcode.snout.R
import com.nightcode.snout.widgets.WidgetState

/**
 * A widget with nothing to show, saying why under its [title] and [caption]; a tap does what the message offers:
 * tries again ([retry]) or opens the app to fix the connection.
 */
@Composable
internal fun WidgetNotice(
    state: WidgetState<*>,
    title: String,
    caption: String?,
    load: WidgetLoad<*, *>,
    retry: Action,
) {
    val openApp = openInApp(LocalContext.current, link = null)
    val (message, hint, onClick) = when (state) {
        is WidgetState.Failed -> if (state.error.isTransient) {
            Triple(string(state.error.widgetMessage()), string(R.string.widget_retry), retry)
        } else {
            Triple(string(state.error.widgetMessage()), string(R.string.widget_open_app), openApp)
        }

        WidgetState.SignedOut, is WidgetState.Loaded -> Triple(string(R.string.widget_signed_out), null, openApp)
    }
    WidgetMessageFrame(title, caption, message, hint, onClick, retry, load)
}

/** [message] centred under the widget's [title] and [caption], with the refresh button. */
@Composable
internal fun WidgetMessageFrame(
    title: String,
    caption: String?,
    message: String,
    hint: String?,
    onClick: Action,
    retry: Action,
    load: WidgetLoad<*, *>,
) {
    val compact = LocalSize.current.height < CompactHeight
    WidgetFrame(onClick) {
        WidgetHeader(title, caption.takeUnless { compact }, retry, load.isReloading)
        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.Center) {
            WidgetMessage(message, hint, compact)
        }
    }
}
