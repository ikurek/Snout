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
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import com.nightcode.snout.designsystem.tokens.Spacing
import com.nightcode.snout.shared.common.format.ChangeTone
import com.nightcode.snout.shared.common.format.trendChange

/**
 * Signed period-over-period change, written as [trendChange] writes it for the widgets too. [lowerIsBetter] flips
 * which direction counts as good (bounce rate, errors).
 */
@Composable
internal fun TrendChip(changePercent: Double, modifier: Modifier = Modifier, lowerIsBetter: Boolean = false) {
    val change = trendChange(changePercent, lowerIsBetter)
    val (container, content) = when (change.tone) {
        ChangeTone.Neutral -> SnoutTheme.colors.surfaceContainerHigh to SnoutTheme.colors.onSurfaceVariant
        ChangeTone.Good -> SnoutTheme.extendedColors.successContainer to SnoutTheme.extendedColors.onSuccessContainer
        ChangeTone.Bad -> SnoutTheme.colors.errorContainer to SnoutTheme.colors.onErrorContainer
    }
    Surface(modifier = modifier, shape = SnoutTheme.shapes.extraSmall, color = container, contentColor = content) {
        Text(
            text = change.text,
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
