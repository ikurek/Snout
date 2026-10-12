package com.nightcode.snout.designsystem.atoms.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.HorizontalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_logout_24dp

/** Outlined secondary action (Open in PostHog) or, with [destructive], the red Log out button. Leading icon. */
@Composable
internal fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
) {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1.0f)
    val contentColor = if (destructive) SnoutTheme.colors.error else SnoutTheme.colors.primary
    val borderColor = if (enabled) contentColor else SnoutTheme.colors.outlineVariant
    OutlinedButton(
        onClick = onClick,
        shape = SnoutTheme.shapes.medium,
        modifier = Modifier.defaultMinSize(minHeight = ButtonHeight * fontScale).then(modifier),
        enabled = enabled && loading.not(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonProgressSize * fontScale),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(painter = it, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
                    HorizontalSpacer(Spacing.small)
                }
                Text(text)
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SecondaryButtonPreview() = SecondaryButton(text = "Open in PostHog", onClick = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun SecondaryButtonDestructivePreview() = SecondaryButton(
    text = "Log out",
    onClick = {},
    destructive = true,
    icon = painterResource(Res.drawable.ic_logout_24dp),
)
