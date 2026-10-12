package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.atoms.buttons.PrimaryButton
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_sticky_note_24dp

/**
 * Full-area state for a screen with nothing to render: icon tile, title, one sentence, one filled button.
 * Loading is never a placeholder; that is skeleton rows.
 */
@Composable
internal fun Placeholder(
    icon: Painter,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = SnoutTheme.shapes.large,
            color = SnoutTheme.colors.surfaceContainerHigh,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(Spacing.large).size(24.dp),
                tint = SnoutTheme.colors.onSurfaceVariant,
            )
        }
        VerticalSpacer(Spacing.large)
        Text(text = title, style = SnoutTheme.typography.titleMedium, textAlign = TextAlign.Center)
        VerticalSpacer(Spacing.xxSmall)
        Text(
            text = message,
            style = SnoutTheme.typography.bodyMedium,
            color = SnoutTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(Spacing.xLarge)
        PrimaryButton(text = actionLabel, onClick = onAction)
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun PlaceholderPreview() = Placeholder(
    icon = painterResource(Res.drawable.ic_sticky_note_24dp),
    title = "No annotations yet",
    message = "Annotations mark releases and incidents on every chart.",
    actionLabel = "Refresh",
    onAction = {},
)
