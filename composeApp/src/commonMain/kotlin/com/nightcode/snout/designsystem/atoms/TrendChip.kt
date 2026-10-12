package com.nightcode.snout.designsystem.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatDecimals
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import kotlin.math.abs

private const val FlatThresholdPercent = 0.05
private const val Up = "↑"
private const val Down = "↓"
private const val Flat = "→"

/**
 * Signed period-over-period change. The arrow is always drawn so colour is never the only signal;
 * [lowerIsBetter] flips which direction counts as good (bounce rate, errors).
 */
@Composable
internal fun TrendChip(changePercent: Double, modifier: Modifier = Modifier, lowerIsBetter: Boolean = false) {
    val flat = abs(changePercent) < FlatThresholdPercent
    val improved = if (lowerIsBetter) changePercent < 0 else changePercent > 0
    val (container, content) = when {
        flat -> SnoutTheme.colors.surfaceContainerHigh to SnoutTheme.colors.onSurfaceVariant
        improved -> SnoutTheme.extendedColors.successContainer to SnoutTheme.extendedColors.onSuccessContainer
        else -> SnoutTheme.colors.errorContainer to SnoutTheme.colors.onErrorContainer
    }
    val arrow = when {
        flat -> Flat
        changePercent > 0 -> Up
        else -> Down
    }
    Surface(modifier = modifier, shape = SnoutTheme.shapes.extraSmall, color = container, contentColor = content) {
        Text(
            text = "$arrow ${abs(changePercent).formatDecimals(1)}%",
            style = SnoutTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = Spacing.xSmall, vertical = Spacing.xxxSmall),
        )
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun TrendChipUpPreview() = TrendChip(changePercent = 12.8)

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun TrendChipDownPreview() = TrendChip(changePercent = -5.5)
