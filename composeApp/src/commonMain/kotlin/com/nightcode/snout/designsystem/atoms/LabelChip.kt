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

/** Semantic color of a [LabelChip]; statuses map onto these so every screen colors them the same way. */
internal enum class LabelChipTone {
    Neutral,
    Success,
    Warning,
    Error,
    Info,
}

/** Compact non-interactive label for tags, kinds and statuses. */
@Composable
internal fun LabelChip(text: String, modifier: Modifier = Modifier, tone: LabelChipTone = LabelChipTone.Neutral) {
    val (container, content) = tone.colors()
    Surface(modifier = modifier, shape = SnoutTheme.shapes.extraSmall, color = container, contentColor = content) {
        Text(
            text = text,
            style = SnoutTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun LabelChipTone.colors(): Pair<Color, Color> {
    val extended = SnoutTheme.extendedColors
    val colors = SnoutTheme.colors
    return when (this) {
        LabelChipTone.Neutral -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        LabelChipTone.Success -> extended.successContainer to extended.onSuccessContainer
        LabelChipTone.Warning -> extended.warningContainer to colors.onSurface
        LabelChipTone.Error -> colors.errorContainer to colors.onErrorContainer
        LabelChipTone.Info -> colors.primaryContainer to colors.onPrimaryContainer
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LabelChipPreview() = LabelChip(text = "Trends")

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun LabelChipSuccessPreview() = LabelChip(text = "Active", tone = LabelChipTone.Success)
