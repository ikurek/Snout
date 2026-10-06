package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private const val BarAlpha = 0.14f

@Immutable
internal data class RankedBarRow(val label: String, val value: Double, val secondary: String? = null)

/** Rows with a proportional background bar; labels are paths or event names, so they read in code style. */
@Composable
internal fun RankedBarList(
    rows: List<RankedBarRow>,
    modifier: Modifier = Modifier,
    color: Color = chartColor(0),
    valueFormatter: (Double) -> String = { it.formatCompact() },
) {
    val max = rows.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1.0
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Box(modifier = Modifier.fillMaxWidth().height(36.dp).clip(SnoutTheme.shapes.extraSmall)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((row.value / max).toFloat().coerceIn(0f, 1f))
                        .background(color.copy(alpha = BarAlpha)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = row.label,
                        style = SnoutTheme.code.medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    row.secondary?.let {
                        Text(
                            text = it,
                            style = SnoutTheme.typography.bodySmall,
                            color = SnoutTheme.colors.onSurfaceVariant,
                        )
                    }
                    Text(text = valueFormatter(row.value), style = SnoutTheme.typography.titleSmall)
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun RankedBarListPreview() = RankedBarList(
    rows = listOf(
        RankedBarRow("/", 8200.0, "20.1k views"),
        RankedBarRow("/pricing", 3100.0, "5.4k views"),
        RankedBarRow("/docs/getting-started", 1900.0, "4.2k views"),
    ),
)
