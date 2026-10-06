package com.nightcode.snout.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.nightcode.snout.common.format.formatSigned
import com.nightcode.snout.designsystem.tokens.SnoutTheme
import org.jetbrains.compose.resources.painterResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.ic_trending_down_24dp
import snout.composeapp.generated.resources.ic_trending_flat_24dp
import snout.composeapp.generated.resources.ic_trending_up_24dp

private const val FlatThresholdPercent = 0.05

/** Big number with label and an optional period-over-period delta. */
@Composable
internal fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    changePercent: Double? = null,
    isIncreaseBad: Boolean = false,
    secondary: String? = null,
) {
    Surface(
        modifier = modifier,
        shape = SnoutTheme.shapes.medium,
        color = SnoutTheme.colors.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = SnoutTheme.typography.labelMedium,
                color = SnoutTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = value, style = SnoutTheme.typography.headlineMedium, maxLines = 1)
            if (changePercent != null) {
                TrendDelta(changePercent = changePercent, isIncreaseBad = isIncreaseBad, secondary = secondary)
            } else if (secondary != null) {
                Text(
                    text = secondary,
                    style = SnoutTheme.typography.labelSmall,
                    color = SnoutTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrendDelta(changePercent: Double, isIncreaseBad: Boolean, secondary: String?) {
    val flat = kotlin.math.abs(changePercent) < FlatThresholdPercent
    val positive = if (isIncreaseBad) changePercent < 0 else changePercent > 0
    val color = when {
        flat -> SnoutTheme.colors.onSurfaceVariant
        positive -> SnoutTheme.extendedColors.success
        else -> SnoutTheme.colors.error
    }
    val icon = when {
        flat -> Res.drawable.ic_trending_flat_24dp
        changePercent > 0 -> Res.drawable.ic_trending_up_24dp
        else -> Res.drawable.ic_trending_down_24dp
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text = "${changePercent.formatSigned()}%", style = SnoutTheme.typography.labelMedium, color = color)
        secondary?.let {
            Text(
                text = it,
                style = SnoutTheme.typography.labelSmall,
                color = SnoutTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun MetricCardPreview() = MetricCard(
    label = "Visitors",
    value = "12.4k",
    changePercent = 12.8,
    secondary = "vs 11k",
)
