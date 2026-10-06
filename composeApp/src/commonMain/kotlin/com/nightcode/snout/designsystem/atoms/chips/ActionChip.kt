package com.nightcode.snout.designsystem.atoms.chips

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** Preset shortcut under a form field ("Cloud EU"): `outline` border, square-ish corners. */
@Composable
internal fun ActionChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        enabled = enabled,
        shape = SnoutTheme.shapes.extraSmall,
        colors = AssistChipDefaults.assistChipColors(labelColor = SnoutTheme.colors.onSurface),
        border = AssistChipDefaults.assistChipBorder(enabled = enabled, borderColor = SnoutTheme.colors.outline),
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun ActionChipPreview() = ActionChip(text = "Cloud EU", onClick = {})
