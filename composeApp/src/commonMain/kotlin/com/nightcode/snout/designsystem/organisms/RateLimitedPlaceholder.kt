package com.nightcode.snout.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.molecules.Placeholder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_schedule_24dp
import snout.composeapp.generated.resources.placeholder_rate_limited_retry
import snout.composeapp.generated.resources.placeholder_rate_limited_subtitle
import snout.composeapp.generated.resources.placeholder_rate_limited_title

@Composable
internal fun RateLimitedPlaceholder(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Placeholder(
        icon = painterResource(Res.drawable.ic_schedule_24dp),
        title = stringResource(Res.string.placeholder_rate_limited_title),
        message = stringResource(Res.string.placeholder_rate_limited_subtitle),
        actionLabel = stringResource(Res.string.placeholder_rate_limited_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun RateLimitedPlaceholderPreview() = RateLimitedPlaceholder(onRetry = {})
