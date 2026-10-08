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
import com.nightcode.snout.common.format.formatDecimals
import com.nightcode.snout.designsystem.tokens.SnoutTheme

private val LabelWidth = 52.dp
private val SizeWidth = 44.dp
private val CellWidth = 46.dp
private val RowHeight = 32.dp
private const val MaxShadeAlpha = 0.6f
private const val WholePercentFrom = 0.1
private const val Hundred = 100

/** [rates] are 0..1 per interval; null where the cohort hasn't reached that interval yet. */
@Immutable
internal data class RetentionTableRow(val label: String, val size: Double, val rates: List<Double?>)

/**
 * Cohort by interval grid, each cell shaded by the share of the cohort still active. The cohort and size columns
 * stay put while the intervals scroll sideways.
 */
@Composable
internal fun RetentionTable(
    sizeLabel: String,
    intervals: List<String>,
    rows: List<RetentionTableRow>,
    modifier: Modifier = Modifier,
) {
    val shade = chartColor(0)
    Row(modifier = modifier.fillMaxWidth()) {
        Column {
            Row {
                HeaderCell(text = "", width = LabelWidth)
                HeaderCell(text = sizeLabel, width = SizeWidth)
            }
            Divider(LabelWidth + SizeWidth)
            rows.forEach { row ->
                Row(modifier = Modifier.height(RowHeight), verticalAlignment = Alignment.CenterVertically) {
                    BodyCell(text = row.label, width = LabelWidth)
                    BodyCell(text = row.size.formatCompact(), width = SizeWidth)
                }
            }
        }
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row { intervals.forEach { HeaderCell(text = it, width = CellWidth) } }
            Divider(CellWidth * intervals.size)
            rows.forEach { row ->
                Row {
                    intervals.indices.forEach { index ->
                        val rate = row.rates.getOrNull(index)
                        val alpha = (rate ?: 0.0).toFloat().coerceIn(0f, 1f) * MaxShadeAlpha
                        Box(
                            modifier = Modifier
                                .width(CellWidth)
                                .height(RowHeight)
                                .padding(1.dp)
                                .background(shade.copy(alpha = alpha)),
                            contentAlignment = Alignment.Center,
                        ) {
                            rate?.let { Text(text = it.formatRate(), style = SnoutTheme.code.small, maxLines = 1) }
                        }
                    }
                }
            }
        }
    }
}

/** Sized explicitly: inside the sideways scroll there is no width for the divider to fill. */
@Composable
private fun Divider(width: Dp) {
    HorizontalDivider(modifier = Modifier.width(width), color = SnoutTheme.colors.outlineVariant)
}

/** At most four characters so cells stay narrow: "100%", "48%", "8.9%". */
private fun Double.formatRate(): String = "${(this * Hundred).formatDecimals(if (this >= WholePercentFrom) 0 else 1)}%"

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        style = SnoutTheme.typography.labelMedium,
        color = SnoutTheme.colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width).padding(vertical = 6.dp, horizontal = 2.dp),
    )
}

@Composable
private fun BodyCell(text: String, width: Dp) {
    Text(
        text = text,
        style = SnoutTheme.code.small,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width).padding(horizontal = 2.dp),
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
