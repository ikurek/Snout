package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * Semantic tone shared by [StatusPill] and [Tag]. Status is never colour alone: the word always travels with it.
 * `primary` is deliberately absent; orange means actionable, not "on".
 */
internal enum class StatusTone {
    Neutral,
    Active,
    Attention,
    Info,
}

/** Lifecycle state of a flag, experiment or insight: a fully rounded pill, 22dp tall. */
@Composable
internal fun StatusPill(text: String, modifier: Modifier = Modifier, tone: StatusTone = StatusTone.Neutral) {
    val (container, content) = tone.colors()
    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = text,
            style = SnoutTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

/** Type or category label ("Trends", "mobile"): a square-cornered tag, 18dp tall. */
@Composable
internal fun Tag(text: String, modifier: Modifier = Modifier, tone: StatusTone = StatusTone.Neutral) {
    val (container, content) = tone.colors()
    Surface(modifier = modifier, shape = SnoutTheme.shapes.extraSmall, color = container, contentColor = content) {
        Text(
            text = text,
            style = SnoutTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
        )
    }
}

@Composable
private fun StatusTone.colors(): Pair<Color, Color> {
    val colors = SnoutTheme.colors
    val extended = SnoutTheme.extendedColors
    return when (this) {
        StatusTone.Neutral -> colors.surfaceContainerHigh to colors.onSurfaceVariant
        StatusTone.Active -> extended.successContainer to extended.onSuccessContainer
        StatusTone.Attention -> colors.errorContainer to colors.onErrorContainer
        StatusTone.Info -> colors.tertiaryContainer to colors.onTertiaryContainer
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun StatusPillActivePreview() = StatusPill(text = "Active", tone = StatusTone.Active)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun StatusPillNeutralPreview() = StatusPill(text = "Inactive")

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun TagPreview() = Tag(text = "Trends")
