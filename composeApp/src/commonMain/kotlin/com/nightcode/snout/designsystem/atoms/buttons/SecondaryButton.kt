package com.nightcode.snout.designsystem.atoms.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.HorizontalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
) {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1.0f)
    val contentColor = if (destructive) SnoutTheme.colors.error else SnoutTheme.colors.primary
    OutlinedButton(
        onClick = onClick,
        shapes = ButtonShapes(shape = SnoutTheme.shapes.large, pressedShape = SnoutTheme.shapes.small),
        modifier = Modifier.defaultMinSize(minHeight = 48.dp * fontScale).then(modifier),
        enabled = enabled && loading.not(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = BorderStroke(1.dp, if (enabled) contentColor else SnoutTheme.colors.outlineVariant),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp * fontScale), color = contentColor)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    icon()
                    HorizontalSpacer(8.dp)
                }
                Text(text)
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun SecondaryButtonPreview() = SecondaryButton(text = "Button text", onClick = {})

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
private fun SecondaryButtonDestructivePreview() = SecondaryButton(text = "Log out", onClick = {}, destructive = true)
