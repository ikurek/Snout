package com.nightcode.snout.appwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.nightcode.snout.MainActivity
import com.nightcode.snout.R
import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.TrendChange
import com.nightcode.snout.shared.domain.model.common.DataError
import java.text.DateFormat

/** Inside the frame, on every side. */
internal val WidgetPadding = 14.dp

/** Below this height a widget is one cell tall: a header and a value, nothing more. */
internal val CompactHeight = 100.dp

/** From this width a widget has room for a second column. */
internal val WideWidth = 250.dp

/** What the header and the footer take, for sizing what goes between them. */
internal val HeaderHeight = 36.dp
internal val FooterHeight = 18.dp

private val RefreshTarget = 32.dp
private val RefreshIcon = 18.dp

/** Top and bottom inset of a widget one cell tall, which has room for its header and number only with less. */
private val CompactVerticalPadding = 8.dp

/** A frame's inset: [WidgetPadding] on every side, less at the top and bottom of a widget one cell tall. */
@Composable
internal fun GlanceModifier.framePadding(): GlanceModifier = if (LocalSize.current.height < CompactHeight) {
    padding(horizontal = WidgetPadding, vertical = CompactVerticalPadding)
} else {
    padding(WidgetPadding)
}

/**
 * The frame every Snout widget draws in: the app's background, doing [onClick] when tapped anywhere. Its content is
 * inset by [WidgetPadding] unless it isn't [padded]: a widget with a [WidgetChart] insets the rest itself.
 */
@Composable
internal fun WidgetFrame(onClick: Action, padded: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val shape = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // The launcher's own radius, which it clips widgets to.
        GlanceModifier.background(WidgetColors.background)
            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        GlanceModifier.background(ImageProvider(R.drawable.widget_background))
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .then(shape)
            .then(if (padded) GlanceModifier.framePadding() else GlanceModifier)
            .clickable(onClick),
        content = content,
    )
}

/**
 * The widget's [title] over a [caption], with a refresh button at the end: a spinner while [isReloading], nothing
 * without [onRefresh].
 */
@Composable
internal fun WidgetHeader(title: String, caption: String?, onRefresh: Action?, isReloading: Boolean) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = title,
                maxLines = 1,
                style = TextStyle(color = WidgetColors.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium),
            )
            caption?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    style = TextStyle(color = WidgetColors.onSurfaceVariant, fontSize = 12.sp),
                )
            }
        }
        RefreshButton(onRefresh, isReloading)
    }
}

@Composable
private fun RefreshButton(onRefresh: Action?, isReloading: Boolean) {
    // Pulled into the frame's padding, so the icon lines up with the title rather than the edge.
    val modifier = GlanceModifier.size(RefreshTarget).padding((RefreshTarget - RefreshIcon) / 2)
    when {
        isReloading -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = GlanceModifier.size(RefreshIcon), color = WidgetColors.chart)
        }

        onRefresh != null -> Image(
            provider = ImageProvider(R.drawable.ic_widget_refresh),
            contentDescription = string(R.string.widget_refresh),
            colorFilter = ColorFilter.tint(WidgetColors.onSurfaceVariant),
            modifier = modifier.clickable(onRefresh),
        )
    }
}

@Composable
internal fun WidgetValue(text: String, fontSize: TextUnit = 32.sp) = Text(
    text = text,
    maxLines = 1,
    style = TextStyle(color = WidgetColors.onBackground, fontSize = fontSize, fontWeight = FontWeight.Bold),
)

/** The change as the app's trend chips write it, coloured by whether it is good news. */
@Composable
internal fun WidgetChangeText(change: TrendChange, fontSize: TextUnit = 12.sp) = Text(
    text = change.text,
    maxLines = 1,
    style = TextStyle(
        color = when (change.tone) {
            ChangeTone.Good -> WidgetColors.good
            ChangeTone.Bad -> WidgetColors.bad
            ChangeTone.Neutral -> WidgetColors.onSurfaceVariant
        },
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
    ),
)

/** A small grey line, such as a stat's name over its value. */
@Composable
internal fun WidgetCaption(text: String, modifier: GlanceModifier = GlanceModifier) = Text(
    text = text,
    maxLines = 1,
    modifier = modifier,
    style = TextStyle(color = WidgetColors.onSurfaceVariant, fontSize = 11.sp),
)

/** When the widget last loaded: the time today, the date before. */
@Composable
internal fun WidgetFooter(loadedAt: Long) {
    val time = DateUtils.formatSameDayTime(loadedAt, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT)
    WidgetCaption(
        text = LocalContext.current.getString(R.string.widget_updated, time),
        modifier = GlanceModifier.padding(top = 4.dp),
    )
}

/** Why there is nothing to show, centred; [hint] says what a tap does about it. */
@Composable
internal fun WidgetMessage(message: String, hint: String?, compact: Boolean) {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            maxLines = if (compact) 2 else 3,
            style = TextStyle(color = WidgetColors.onBackground, fontSize = 13.sp, textAlign = TextAlign.Center),
            modifier = GlanceModifier.fillMaxWidth(),
        )
        if (hint != null && !compact) {
            Text(
                text = hint,
                maxLines = 1,
                style = TextStyle(
                    color = WidgetColors.chart,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
                modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

/** From this height a widget one cell tall has room for the project under its title. */
private val CompactCaptionHeight = 92.dp

/**
 * The header of a widget one cell tall: [title] over the [project] when there is room for both, or the project before
 * the title on one line when there isn't, so the widget always says which project it shows.
 */
@Composable
internal fun CompactHeader(title: String, project: String?, load: WidgetLoad<*, *>, refresh: Action) {
    if (LocalSize.current.height >= CompactCaptionHeight.scaled()) {
        WidgetHeader(title, project, refresh, load.isReloading)
    } else {
        WidgetHeader(withProject(project, title), caption = null, onRefresh = refresh, isReloading = load.isReloading)
    }
}

/** [text] after the [project]'s name, when the widget knows it (one set up before it kept the name doesn't). */
@Composable
internal fun withProject(project: String?, text: String): String =
    project?.let { LocalContext.current.getString(R.string.widget_title_with_range, it, text) } ?: text

/** Opens [link] in the app, which shows its screen as it would a PostHog link; just the app without one. */
internal fun openInApp(context: Context, link: String?): Action = actionStartActivity(
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (link != null) {
            action = Intent.ACTION_VIEW
            data = link.toUri()
        }
    },
)

/** Opens the widget's settings, as the launcher's "Reconfigure" does. */
internal fun configureAction(context: Context, activity: Class<out Activity>, appWidgetId: Int): Action =
    actionStartActivity(
        Intent(context, activity)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

/** Whether trying again can help, or the person has to fix something in the app first. */
internal val DataError.isTransient: Boolean
    get() = this in setOf(DataError.RateLimited, DataError.ConnectionError, DataError.UnexpectedError)

@StringRes
internal fun DataError.widgetMessage(): Int = when (this) {
    DataError.Unauthorized -> R.string.widget_error_unauthorized
    DataError.Forbidden -> R.string.widget_error_forbidden
    DataError.PaymentRequired -> R.string.widget_error_payment_required
    DataError.NotFound -> R.string.widget_error_not_found
    DataError.RateLimited -> R.string.widget_error_rate_limited
    DataError.ConnectionError -> R.string.widget_error_offline
    DataError.UnexpectedError -> R.string.widget_error_unexpected
}

@Composable
internal fun string(@StringRes id: Int): String = LocalContext.current.getString(id)
