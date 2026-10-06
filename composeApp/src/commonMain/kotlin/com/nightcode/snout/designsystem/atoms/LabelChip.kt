package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** Compact non-interactive label for tags, kinds and statuses. */
@Composable
internal fun LabelChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SnoutTheme.colors.surfaceContainerHighest,
    contentColor: Color = SnoutTheme.colors.onSurfaceVariant,
) {
    Surface(
        modifier = modifier,
        shape = SnoutTheme.shapes.extraSmall,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = SnoutTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LabelChipPreview() = LabelChip(text = "Trends")
