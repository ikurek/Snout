package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.designsystem.tokens.SnoutTheme

@Immutable
internal data class FunnelBarStep(
    val label: String,
    val value: Double,
    val fraction: Double,
    val caption: String? = null,
)

/** Funnel steps as full-width bars whose fill is the share of the first step. */
@Composable
internal fun FunnelBars(
    steps: List<FunnelBarStep>,
    modifier: Modifier = Modifier,
    valueFormatter: (Double) -> String = { it.formatCompact() },
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEachIndexed { index, step ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${index + 1}. ${step.label}",
                        style = SnoutTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = valueFormatter(step.value), style = SnoutTheme.code.medium)
                    Text(
                        text = step.fraction.formatPercent(isFraction = true),
                        style = SnoutTheme.typography.labelLarge,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(SnoutTheme.shapes.extraSmall)
                        .background(SnoutTheme.colors.surfaceContainerHighest),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(step.fraction.toFloat().coerceIn(0f, 1f))
                            .background(chartColor(index)),
                    )
                }
                step.caption?.let {
                    Text(
                        text = it,
                        style = SnoutTheme.typography.labelSmall,
                        color = SnoutTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun FunnelBarsPreview() = FunnelBars(
    steps = listOf(
        FunnelBarStep("Visited pricing", 1200.0, 1.0),
        FunnelBarStep("Started checkout", 480.0, 0.4, "median 1m 35s"),
        FunnelBarStep("Paid", 210.0, 0.175, "median 5m"),
    ),
)
