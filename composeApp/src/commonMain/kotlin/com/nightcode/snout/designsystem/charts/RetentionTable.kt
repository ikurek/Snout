package com.nightcode.snout.designsystem.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nightcode.snout.common.compose.ThemePreviewWrapper
import com.nightcode.snout.common.format.formatCompact
import com.nightcode.snout.common.format.formatPercent
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private val LabelWidth = 72.dp
private val CellWidth = 60.dp
private val RowHeight = 32.dp
private const val MaxShadeAlpha = 0.6f

/** [rates] are 0..1 per interval; null where the cohort hasn't reached that interval yet. */
@Immutable
internal data class RetentionTableRow(val label: String, val size: Double, val rates: List<Double?>)

/** Cohort by interval grid, each cell shaded by the share of the cohort still active. */
@Composable
internal fun RetentionTable(
    sizeLabel: String,
    intervals: List<String>,
    rows: List<RetentionTableRow>,
    modifier: Modifier = Modifier,
) {
    val shade = chartColor(0)
    Column(modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row {
            HeaderCell(text = "", width = LabelWidth)
            HeaderCell(text = sizeLabel, width = CellWidth)
            intervals.forEach { HeaderCell(text = it, width = CellWidth) }
        }
        HorizontalDivider(color = SnoutTheme.colors.outlineVariant)
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                BodyCell(text = row.label, width = LabelWidth)
                BodyCell(text = row.size.formatCompact(), width = CellWidth)
                intervals.indices.forEach { index ->
                    val rate = row.rates.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .width(CellWidth)
                            .height(RowHeight)
                            .padding(1.dp)
                            .background(shade.copy(alpha = (rate ?: 0.0).toFloat().coerceIn(0f, 1f) * MaxShadeAlpha)),
                        contentAlignment = Alignment.Center,
                    ) {
                        rate?.let { Text(text = it.formatPercent(isFraction = true), style = SnoutTheme.code.small) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        style = SnoutTheme.typography.labelMedium,
        color = SnoutTheme.colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width).padding(vertical = 6.dp, horizontal = 4.dp),
    )
}

@Composable
private fun BodyCell(text: String, width: Dp) {
    Text(
        text = text,
        style = SnoutTheme.code.small,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width).padding(horizontal = 4.dp),
    )
}

@PreviewLightDark
@PreviewWrapper(ThemePreviewWrapper::class)
@Composable
fun RetentionTablePreview() = RetentionTable(
    sizeLabel = "Users",
    intervals = listOf("Day 0", "Day 1", "Day 2", "Day 3"),
    rows = listOf(
        RetentionTableRow("1 Oct", 400.0, listOf(1.0, 0.45, 0.3, 0.24)),
        RetentionTableRow("2 Oct", 360.0, listOf(1.0, 0.42, 0.28)),
        RetentionTableRow("3 Oct", 420.0, listOf(1.0, 0.48)),
        RetentionTableRow("4 Oct", 380.0, listOf(1.0)),
    ),
)
