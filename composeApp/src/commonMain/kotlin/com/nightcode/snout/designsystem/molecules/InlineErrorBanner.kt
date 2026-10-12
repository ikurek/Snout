package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_warning_outlined_24dp
import snout.composeapp.generated.resources.inline_error_retry

/**
 * Strip for a recoverable failure that leaves the rest of the screen usable: a refresh that failed, a tile
 * that could not compute, a refused toggle. Loaded content stays visible beneath it.
 */
@Composable
internal fun InlineErrorBanner(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String = stringResource(Res.string.inline_error_retry),
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.errorContainer,
        contentColor = SnoutTheme.colors.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_warning_outlined_24dp),
                contentDescription = null,
                modifier = Modifier.size(BannerIconSize),
            )
            Text(text = text, style = SnoutTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (onAction != null) {
                BannerAction(label = actionLabel, onClick = onAction)
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun InlineErrorBannerPreview() = InlineErrorBanner(text = "Couldn't load your account.", onAction = {})
