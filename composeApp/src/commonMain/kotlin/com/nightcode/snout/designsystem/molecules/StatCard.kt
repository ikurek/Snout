package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.designsystem.atoms.TrendChip
import com.nightcode.snout.designsystem.atoms.VerticalSpacer
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/**
 * One headline metric: label, pre-formatted value in tabular figures, and the period-over-period change as a
 * [TrendChip] beside the comparison text. The card does no math and never colours the value itself.
 */
@Composable
internal fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    changePercent: Double? = null,
    lowerIsBetter: Boolean = false,
    comparison: String? = null,
) {
    Surface(modifier = modifier, shape = SnoutTheme.shapes.medium, color = SnoutTheme.colors.surfaceContainerLow) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = SnoutTheme.typography.bodySmall,
                color = SnoutTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            VerticalSpacer(4.dp)
            Text(text = value, style = SnoutTheme.typography.displaySmall, maxLines = 1)
            if (changePercent != null || comparison != null) {
                VerticalSpacer(8.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    changePercent?.let { TrendChip(changePercent = it, lowerIsBetter = lowerIsBetter) }
                    comparison?.let {
                        Text(
                            text = it,
                            style = SnoutTheme.typography.bodySmall,
                            color = SnoutTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun StatCardPreview() = StatCard(label = "Visitors", value = "12.4k", changePercent = 12.8, comparison = "vs 11k")

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun StatCardDownPreview() = StatCard(label = "Page views", value = "48.2k", changePercent = -5.5, comparison = "vs 51k")
