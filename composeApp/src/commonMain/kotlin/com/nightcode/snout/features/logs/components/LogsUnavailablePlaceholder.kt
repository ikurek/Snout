package com.nightcode.snout.features.logs.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nightcode.snout.designsystem.organisms.UnavailablePlaceholder
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.logs_unavailable_subtitle
import snout.composeapp.generated.resources.logs_unavailable_title

/** PostHog answers 404 for the Logs API on instances that predate it. */
@Composable
internal fun LogsUnavailablePlaceholder(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    UnavailablePlaceholder(
        title = stringResource(Res.string.logs_unavailable_title),
        message = stringResource(Res.string.logs_unavailable_subtitle),
        onRetry = onRetry,
        modifier = modifier,
    )
}
